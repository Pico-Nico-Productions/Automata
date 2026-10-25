package pro.piconico.automata.client.ui.component;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import org.apache.commons.lang3.function.TriConsumer;
import io.wispforest.owo.ui.component.TextBoxComponent;
import io.wispforest.owo.ui.component.UIComponents;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.UIContainers;
import io.wispforest.owo.ui.core.HorizontalAlignment;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.VerticalAlignment;
import net.minecraft.text.Text;
import pro.piconico.automata.bot.team.BotTeam;
import pro.piconico.automata.bot.team.BotTeam.BotTeamSettings;
import pro.piconico.automata.network.message.BotTeamCreateC2SMessage;
import pro.piconico.automata.network.message.BotTeamDeleteC2SMessage;
import pro.piconico.automata.network.message.BotTeamUpdateC2SMessage;
import pro.piconico.automata.registry.AutomataMessages;
import pro.piconico.automata.registry.AutomataTexts;
import pro.piconico.automata.screen.BotDeviceScreenHandler;

public class BotTeamSettingsCrudForm {
    public enum Mutation {
        CREATE(AutomataTexts.getCreate(), (playerUuid, team, settings) -> {
            if (!BotTeam.isValidName(settings.name()))
                return;

            AutomataMessages.BOT_DEVICE_CHANNEL.clientHandle().send(new BotTeamCreateC2SMessage(settings, playerUuid));
        }), //
        UPDATE(AutomataTexts.getUpdate(), (playerUuid, team, settings) -> {
            if (team.isEmpty() || !BotTeam.isValidName(settings.name()))
                return;

            AutomataMessages.BOT_DEVICE_CHANNEL.clientHandle().send(new BotTeamUpdateC2SMessage(team.get().uuid, settings));
        }), //
        DELETE(AutomataTexts.getDelete(), (playerUuid, team, settings) -> {
            if (team.isEmpty())
                return;

            AutomataMessages.BOT_DEVICE_CHANNEL.clientHandle().send(new BotTeamDeleteC2SMessage(team.get().uuid));
        });

        private Text name;
        private TriConsumer<UUID, Optional<BotTeam>, BotTeamSettings> mutate;

        private Mutation(Text name, TriConsumer<UUID, Optional<BotTeam>, BotTeamSettings> action) {
            this.name = name;
            this.mutate = action;
        }
    }

    public static void build(BotDeviceScreenHandler handler, FlowLayout parent, Text headerText, Mutation... actions) {
        parent.child(AutomataUIComponents.centerHeader(headerText));

        Optional<BotTeam> team = handler.getTeam();
        boolean requiresTeam = Arrays.stream(actions).noneMatch(Mutation.CREATE::equals);

        if (team.isEmpty() && requiresTeam) {
            parent.child(AutomataUIComponents.centerHeader(AutomataTexts.getTeamEmpty()));
            return;
        }

        FlowLayout nameRow = UIContainers.horizontalFlow(Sizing.fill(), Sizing.content());
        nameRow.verticalAlignment(VerticalAlignment.CENTER);
        nameRow.child(UIComponents.label(AutomataTexts.getName()));
        TextBoxComponent nameTextBox = UIComponents.textBox(Sizing.expand(), team.map(t -> requiresTeam ? t.getName() : null).orElse(""));
        nameRow.child(nameTextBox);
        parent.child(nameRow);

        if (actions.length == 0)
            return;

        FlowLayout actionsRow = UIContainers.horizontalFlow(Sizing.fill(), Sizing.content());
        actionsRow.horizontalAlignment(HorizontalAlignment.CENTER);
        actionsRow.gap(BotDeviceScreenHandler.GAP);
        for (Mutation mutation : actions) {
            actionsRow.child(UIComponents.button(mutation.name, (ignored) -> {
                BotTeamSettings settings = new BotTeamSettings(nameTextBox.getText());
                mutation.mutate.accept(handler.player().getUuid(), team, settings);
            }));
        }
        parent.child(actionsRow);
    }
}
