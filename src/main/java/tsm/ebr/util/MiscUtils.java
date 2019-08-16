package tsm.ebr.util;

/**
 * 未分类的工具函数集合
 * @author catforward
 */
public final class MiscUtils {

    /**
     * 检查对象是否为空
     * 如果为空则抛出空指针异常
     * 否则返回引用给调用者
     * @param obj 检查对象
     * @return Object 检查对象
     */
    public static <T extends Object> T checkNotNull(T obj) {
        if (obj == null) {
            throw new NullPointerException();
        }
        return obj;
    }
}
