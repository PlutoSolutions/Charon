package cc.eventhan;

public interface AttachableEventBus extends EventBus {
   void attach(EventBus var1);

   void detach(EventBus var1);
}
