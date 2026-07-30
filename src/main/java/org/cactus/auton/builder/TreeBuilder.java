package org.cactus.auton.builder;

import org.cactus.auton.exception.BehaviorException;
import org.cactus.auton.exception.NotFoundException;
import org.cactus.auton.node.Node;
import org.cactus.auton.node.composite.Parallel;
import org.cactus.auton.node.composite.Selector;
import org.cactus.auton.node.composite.Sequence;
import org.cactus.auton.node.decorator.Inverter;
import org.cactus.auton.node.decorator.UntilSuccess;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.introspector.PropertyUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 行为树构建器。
 * <p>通过 YAML/JSON 配置或代码编程方式构建 行为树。
 * 采用流式 Builder 模式，按顺序配置 Loader → FactoryRegistry → NodeRegistry，最后 build。
 * 内置 Sequence / Selector / Parallel / Inverter / UntilSuccess 五种节点类型的工厂。</p>
 *
 * @param <S> 业务状态类型
 */
public class TreeBuilder<S> {

    /** 待构建的树配置（由 loader 步骤填充） */
    private TreeNodeConfig config;

    /** 工厂注册表：节点类型名 → NodeFactory */
    private final Map<String, NodeFactory<S>> factoryRegistry;

    /** 类注册表：节点类型名 → Node 实现类（反射创建） */
    private final Map<String, Class<? extends Node<S>>> nodeRegistry;

    public TreeBuilder() {
        this.factoryRegistry = new HashMap<>();
        this.nodeRegistry = new HashMap<>();
        registerBuiltins();
    }

    // ======================== 流式步骤 ========================

    /**
     * 步骤1：加载树配置。
     * <p>通过 {@link Loader} 加载 YAML 文件或字符串，得到一个 {@link TreeNodeConfig}。
     * 也可直接传入已有的 TreeNodeConfig。</p>
     *
     * @param loader 接收 Loader 实例并返回 TreeNodeConfig 的函数
     * @return this
     */
    public TreeBuilder<S> loader(Function<Loader, TreeNodeConfig> loader) {
        this.config = loader.apply(new Loader());
        return this;
    }

    /**
     * 步骤2（可选）：注册节点工厂。
     * <p>通过 {@link NodeFactoryRegistry} 注册自定义节点类型的工厂，优先级高于类注册。
     * 工厂负责完整创建节点（含子节点递归构建）。</p>
     *
     * @param registry 接收 NodeFactoryRegistry 并注册工厂的 Consumer
     * @return this
     */
    public TreeBuilder<S> factoryRegistry(Consumer<NodeFactoryRegistry<S>> registry) {
        registry.accept(new NodeFactoryRegistry<>(factoryRegistry));
        return this;
    }

    /**
     * 步骤3（可选）：注册节点类。
     * <p>通过 {@link NodeRegistry} 注册/扫描 Node 实现类作为反射回退。
     * 当工厂注册表中找不到对应类型时，通过 NodeRegistry 反射创建节点。</p>
     *
     * @param registry 接收 NodeRegistry 并注册/扫描类的 Consumer
     * @return this
     */
    public TreeBuilder<S> nodeRegistry(Consumer<NodeRegistry<S>> registry) {
        registry.accept(new NodeRegistry<>(nodeRegistry));
        return this;
    }

    /**
     * 构建行为树。
     * <p>根据配置递归构建节点树并包装为 BehaviorTree。</p>
     *
     * @return 行为树实例
     */
    public Node<S> build() {
        return buildNode(config);
    }

    // ======================== 内部构建 ========================

    /**
     * 递归构建节点：优先工厂 → 回退反射。
     */
    private Node<S> buildNode(TreeNodeConfig config) {
        NodeFactory<S> factory = factoryRegistry.get(config.getNode());
        return factory != null ? factory.create(config) : defaultFactory(config);
    }

    /**
     * 反射回退：从 NodeRegistry 查找类，反射调用 (String id, String name) 构造器创建。
     */
    private Node<S> defaultFactory(TreeNodeConfig config) {
        Class<? extends Node<S>> cls = nodeRegistry.get(config.getNode());
        if (cls == null) {
            throw new NotFoundException("未知节点类型: " + config.getNode() + " (id=" + config.getId() + ")");
        }
        try {
            java.lang.reflect.Constructor<? extends Node<S>> ctor =
                    cls.getConstructor(String.class, String.class);
            return ctor.newInstance(config.getId(), config.getName());
        } catch (ReflectiveOperationException e) {
            throw new BehaviorException("反射创建节点失败: " + config.getNode(), e);
        }
    }

    // ======================== Loader（YAML 加载器） ========================

    /**
     * YAML 加载器。
     * <p>安全反序列化 YAML 内容为 {@link TreeNodeConfig}，通过
     * SnakeYAML 的 {@link Constructor} + {@link LoaderOptions} 限制反序列化类型。</p>
     */
    public static class Loader {

        private final Yaml YAML = createYaml();

        private Yaml createYaml() {
            LoaderOptions options = new LoaderOptions();

            Constructor constructor =
                    new Constructor(TreeNodeConfig.class, options);
            PropertyUtils propertyUtils = new PropertyUtils();
            propertyUtils.setSkipMissingProperties(true);
            constructor.setPropertyUtils(propertyUtils);
            return new Yaml(constructor);
        }

        /**
         * 从 YAML 输入流加载配置。
         *
         * @param yamlInputStream YAML 输入流
         * @return TreeNodeConfig 配置
         */
        public TreeNodeConfig load(InputStream yamlInputStream) {
            return YAML.load(yamlInputStream);
        }

        /**
         * 从 YAML 字符串加载配置。
         *
         * @param yamlContent YAML 字符串
         * @return TreeNodeConfig 配置
         */
        public TreeNodeConfig load(String yamlContent) {
            return YAML.load(yamlContent);
        }
    }

    // ======================== NodeFactoryRegistry（工厂注册） ========================

    /**
     * 节点工厂注册器。
     * <p>管理自定义节点类型与 {@link NodeFactory} 的映射。
     * 工厂优先级高于 {@link NodeRegistry} 的类注册。</p>
     *
     * @param <S> 业务状态类型
     */
    public static class NodeFactoryRegistry<S> {
        private final Map<String, NodeFactory<S>> registry;

        NodeFactoryRegistry(Map<String, NodeFactory<S>> registry) {
            this.registry = registry;
        }

        /**
         * 注册节点工厂。
         *
         * @param nodeName 节点类型名（对应配置中的 node 字段）
         * @param factory  节点工厂
         * @return this（支持链式调用）
         */
        public NodeFactoryRegistry<S> register(String nodeName, NodeFactory<S> factory) {
            registry.put(nodeName, factory);
            return this;
        }

        /**
         * 查找节点工厂。
         *
         * @param nodeName 节点类型名
         * @return 节点工厂，未注册返回 null
         */
        public NodeFactory<S> get(String nodeName) {
            return registry.get(nodeName);
        }
    }

    // ======================== NodeRegistry（类扫描+注册） ========================

    /**
     * 节点类注册器。
     * <p>管理节点类型名与 {@link Node} 实现类的映射，支持包扫描自动发现。
     * 作为工厂注册的兜底，通过反射 (String id, String name) 构造器创建实例。</p>
     *
     * @param <S> 业务状态类型
     */
    public static class NodeRegistry<S> {
        private final Map<String, Class<? extends Node<S>>> registry;

        NodeRegistry(Map<String, Class<? extends Node<S>>> registry) {
            this.registry = registry;
        }

        /**
         * 扫描指定包路径，将所有非抽象 Node 子类按简单类名注册。
         * <p>支持文件系统 classpath，JAR 包暂不支持。</p>
         *
         * @param basePackage 基包路径，如 "com.myapp.btree.nodes"
         * @return this（支持链式调用）
         */
        public NodeRegistry<S> scanPackages(String basePackage) {
            String path = basePackage.replace('.', '/');
            try {
                Enumeration<URL> resources = Thread.currentThread()
                        .getContextClassLoader().getResources(path);
                while (resources.hasMoreElements()) {
                    URL resource = resources.nextElement();
                    if ("file".equals(resource.getProtocol())) {
                        scanDir(new File(resource.getFile()), basePackage);
                    }
                }
            } catch (IOException e) {
                throw new BehaviorException("扫描包失败: " + basePackage, e);
            }
            return this;
        }

        /**
         * 以简单类名注册节点类。
         *
         * @param cls Node 子类
         * @return this
         */
        public NodeRegistry<S> register(Class<? extends Node<S>> cls) {
            String className = cls.getSimpleName();
            String lowerName = Character.toLowerCase(className.charAt(0)) + className.substring(1);
            registry.put(lowerName, cls);
            return this;
        }

        /**
         * 以指定名称注册节点类。
         *
         * @param name 自定义类型名
         * @param cls  Node 子类
         * @return this
         */
        public NodeRegistry<S> register(String name, Class<? extends Node<S>> cls) {
            registry.put(name, cls);
            return this;
        }


        @SuppressWarnings("unchecked")
        private void scanDir(File dir, String pkg) {
            if (!dir.exists() || !dir.isDirectory()) return;
            File[] files = dir.listFiles();
            if (files == null) return;
            for (File file : files) {
                if (file.isDirectory()) {
                    scanDir(file, pkg + "." + file.getName());
                } else if (file.getName().endsWith(".class")) {
                    String clsName = pkg + "." + file.getName().replace(".class", "");
                    try {
                        Class<?> cls = Class.forName(clsName);
                        if (Node.class.isAssignableFrom(cls)
                                && !Modifier.isAbstract(cls.getModifiers())
                                && !cls.isInterface()) {
                            register((Class<? extends Node<S>>) cls);
                        }
                    } catch (ClassNotFoundException ignored) {
                    }
                }
            }
        }
    }

    // ======================== 内置节点注册 ========================

    /**
     * 注册框架内置节点类型的工厂。
     */
    private void registerBuiltins() {
        NodeFactoryRegistry<S> registry = new NodeFactoryRegistry<>(factoryRegistry);

        registry.register("sequence", config -> {
            Sequence<S> seq = new Sequence<>(config.getId(), config.getName());
            for (TreeNodeConfig child : config.getChildren()) {
                seq.addChild(buildNode(child));
            }
            return seq;
        });

        registry.register("selector", config -> {
            Selector<S> sel = new Selector<>(config.getId(), config.getName());
            for (TreeNodeConfig child : config.getChildren()) {
                sel.addChild(buildNode(child));
            }
            return sel;
        });

        registry.register("parallel", config -> {
            String mode = (String) config.getProps().get("mode");
            Parallel<S> par = mode != null
                    ? new Parallel<>(config.getId(), config.getName(), mode)
                    : new Parallel<>(config.getId(), config.getName());
            for (TreeNodeConfig child : config.getChildren()) {
                par.addChild(buildNode(child));
            }
            return par;
        });

        registry.register("inverter", config -> {
            Inverter<S> inv = new Inverter<>(config.getId(), config.getName());
            if (config.getChildren().size() != 1) {
                throw new BehaviorException("Inverter 子节点数必须为1");
            }
            inv.setChild(buildNode(config.getChildren().get(0)));
            return inv;
        });

        registry.register("untilSuccess", config -> {
            UntilSuccess<S> us = new UntilSuccess<>(config.getId(), config.getName());
            if (config.getChildren().size() != 1) {
                throw new BehaviorException("UntilSuccess 子节点数必须为1");
            }
            us.setChild(buildNode(config.getChildren().get(0)));
            return us;
        });
    }
}
