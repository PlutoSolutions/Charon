package cc.eventhan;

@FunctionalInterface
public interface EventHook<T> {
   void invoke(T var1);
}
