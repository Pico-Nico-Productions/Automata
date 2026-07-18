package pro.piconico.automata.client.event;

import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import pro.piconico.automata.item.CommandToolItem;

public class CommandToolClientEvents {
    public static void initialize() {
        AttackBlockCallback.EVENT.register(CommandToolItem::onAttackBlock);
    }
}
