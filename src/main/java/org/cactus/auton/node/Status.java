package org.cactus.auton.node;

/**
 * 节点执行状态接口。
 * <p>行为树节点执行后返回的状态标记。框架提供 {@link BuiltIn} 内置枚举实现，
 * 用户可通过实现此接口自定义状态。
 * 状态比较应使用 {@link #sameStateAs(Status)} 方法，基于 {@code state()} 返回值进行语义比较，
 * 而非 Object.equals() 的身份比较。</p>
 */
public interface Status {

    /**
     * 获取状态标识名称。
     *
     * @return 状态名称
     */
    String state();

    /**
     * 基于状态名称的语义比较。
     * <p>比较两个状态的 {@code state()} 返回值是否相等，适用于不同 Status 实现类之间的比较。
     * 例如：{@code BuiltIn.SUCCESS.sameStateAs(customStatus)}。</p>
     *
     * @param other 另一个状态
     * @return 如果 other 不为 null 且 state() 相同则返回 true
     */
    default boolean sameStateAs(Status other) {
        if (other == null) {
            return false;
        }
        return this.state().equals(other.state());
    }

    /**
     * 内置状态枚举。
     * <p>覆盖行为树标准的三种状态（SUCCESS / FAILURE / RUNNING），
     * 另加框架特有的 ABORT 状态（版本号不一致导致 tick 中止）。</p>
     */
    enum BuiltIn implements Status {
        SUCCESS,
        FAILURE,
        RUNNING,
        ABORT;

        @Override
        public String state() {
            return this.name();
        }
    }
}
