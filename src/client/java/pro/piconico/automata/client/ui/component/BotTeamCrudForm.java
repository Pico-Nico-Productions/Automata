package pro.piconico.automata.client.ui.component;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.BiConsumer;
import io.wispforest.owo.ui.component.TextBoxComponent;
import io.wispforest.owo.ui.component.UIComponents;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.UIContainers;
import io.wispforest.owo.ui.core.HorizontalAlignment;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.VerticalAlignment;
import net.minecraft.text.Text;
import pro.piconico.automata.bot.team.BotTeam;
import pro.piconico.automata.network.message.BotTeamCreateC2SMessage;
import pro.piconico.automata.network.message.BotTeamDeleteC2SMessage;
import pro.piconico.automata.registry.AutomataMessages;
import pro.piconico.automata.registry.AutomataTexts;
import pro.piconico.automata.screen.BotDeviceScreenHandler;

public class BotTeamCrudForm {
    public enum Action {
        CREATE(AutomataTexts.getCreate(), (team, name) -> {
            if (!BotTeam.isValidName(name))
                return;

            BotTeam newTeam = new BotTeam(name);
            AutomataMessages.BOT_DEVICE_CHANNEL.clientHandle().send(new BotTeamCreateC2SMessage(newTeam));
        }), //
        UPDATE(AutomataTexts.getUpdate(), (team, name) -> {
            if (team.isEmpty() || !BotTeam.isValidName(name))
                return;

            BotTeam newTeam = new BotTeam(name, team.get().UUID);
            team.get().set(newTeam);
        }), //
        DELETE(AutomataTexts.getDelete(), (team, name) -> {
            if (team.isEmpty())
                return;

            AutomataMessages.BOT_DEVICE_CHANNEL.clientHandle().send(new BotTeamDeleteC2SMessage(team.get().UUID));
        });

        private Text text;
        private BiConsumer<Optional<BotTeam>, String> action;

        private Action(Text text, BiConsumer<Optional<BotTeam>, String> action) {
            this.text = text;
            this.action = action;
        }
    }

    public static void build(BotDeviceScreenHandler handler, FlowLayout parent, Text headerText, Action... actions) {
        parent.child(AutomataUIComponents.centerHeader(headerText));

        Optional<BotTeam> team = handler.getTeam();
        boolean requiresTeam = Arrays.stream(actions).noneMatch(Action.CREATE::equals);

        if (team.isEmpty() && requiresTeam) {
            parent.child(AutomataUIComponents.centerHeader(AutomataTexts.getTeamEmpty()));
            return;
        }

        FlowLayout nameRow = UIContainers.horizontalFlow(Sizing.fill(100), Sizing.content());
        nameRow.verticalAlignment(VerticalAlignment.CENTER);
        nameRow.child(UIComponents.label(AutomataTexts.getName()));
        TextBoxComponent nameTextBox = UIComponents.textBox(Sizing.expand(), team.map(t -> requiresTeam ? t.getName() : null).orElse(""));
        nameRow.child(nameTextBox);
        parent.child(nameRow);

        if (actions.length == 0)
            return;

        FlowLayout actionsRow = UIContainers.horizontalFlow(Sizing.fill(100), Sizing.content());
        actionsRow.horizontalAlignment(HorizontalAlignment.CENTER);
        actionsRow.gap(BotDeviceScreenHandler.UI_SPACING);
        for (Action action : actions) {
            actionsRow.child(UIComponents.button(action.text, (ignored) -> {
                String name = nameTextBox.getText();
                action.action.accept(team, name);
            }));
        }
        parent.child(actionsRow);
    }
}
