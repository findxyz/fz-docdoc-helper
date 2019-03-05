package xyz.fz.docdoc.helper.event;

public interface EventListener<E extends Event> {
    void on(E event);
}
