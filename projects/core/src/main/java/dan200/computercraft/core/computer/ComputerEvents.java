// SPDX-FileCopyrightText: 2025 The CC: Tweaked Developers
//
// SPDX-License-Identifier: MPL-2.0

package dan200.computercraft.core.computer;

import javax.annotation.Nullable;

/**
 * Built-in events that can be queued on a computer.
 */
public final class ComputerEvents {
    private ComputerEvents() {
    }

    public static void keyDown(Receiver receiver, int key, boolean repeat) {
        receiver.queueEvent("key", new Object[]{ key, repeat });
    }

    public static void keyUp(Receiver receiver, int key) {
        receiver.queueEvent("key_up", new Object[]{ key });
    }

    public static void mouseClick(Receiver receiver, int button, int x, int y) {
        receiver.queueEvent("mouse_click", new Object[]{ button, x, y });
    }

    public static void mouseUp(Receiver receiver, int button, int x, int y) {
        receiver.queueEvent("mouse_up", new Object[]{ button, x, y });
    }

    public static void mouseDrag(Receiver receiver, int button, int x, int y) {
        receiver.queueEvent("mouse_drag", new Object[]{ button, x, y });
    }

    public static void mouseScroll(Receiver receiver, int direction, int x, int y) {
        receiver.queueEvent("mouse_scroll", new Object[]{ direction, x, y });
    }

    /**
     * An object that can receive computer events.
     */
    @FunctionalInterface
    public interface Receiver {
        void queueEvent(String event, @Nullable Object[] arguments);
    }
}
