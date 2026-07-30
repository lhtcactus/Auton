package org.cactus.auton.builder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 树节点配置。
 * <p>用于描述行为树结构的可序列化 POJO，支持从 YAML/JSON 反序列化。
 * 每个节点包含：
 * <ul>
 *   <li>id — 节点唯一标识</li>
 *   <li>name — 节点名称（可读性描述）</li>
 *   <li>node — 节点类型名，对应 {@link TreeBuilder.NodeFactoryRegistry} 或 {@link TreeBuilder.NodeRegistry} 中的 key</li>
 *   <li>props — 配置属性，传递给工厂用于构造节点参数</li>
 *   <li>children — 子节点列表</li>
 * </ul>
 *
 */
public class TreeNodeConfig {

    /** 节点唯一标识 */
    private String id;

    /** 节点名称 */
    private String name;

    /** 节点类型名 */
    private String node;

    /** 配置属性 */
    private Map<String, Object> props = new HashMap<>();

    /** 子节点列表 */
    private List<TreeNodeConfig> children = new ArrayList<>();

    public TreeNodeConfig() {
    }

    public TreeNodeConfig(String id, String name, String node) {
        this.id = id;
        this.name = name;
        this.node = node;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    /** 节点类型名，对应注册器中的 key */
    public String getNode() { return node; }
    public void setNode(String node) { this.node = node; }

    public Map<String, Object> getProps() { return props; }
    public void setProps(Map<String, Object> props) { this.props = props; }

    public List<TreeNodeConfig> getChildren() { return children; }
    public void setChildren(List<TreeNodeConfig> children) { this.children = children; }
}
