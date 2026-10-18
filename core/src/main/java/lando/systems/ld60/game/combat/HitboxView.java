package lando.systems.ld60.game.combat;

import com.zendogames.zenlib.ecs.components.Name;
import com.zendogames.zenlib.ui.DebugView;
import imgui.ImGui;
import imgui.ImTemp;
import imgui.enums.ImGuiTableColumnFlags;
import imgui.enums.ImGuiTableFlags;
import lando.systems.ld60.game.components.Hitboxes;
import lando.systems.ld60.game.systems.HitSystem;

/**
 * The "Hitboxes" debug section: per fighter its team, the clip and time its boxes were resolved
 * for, and its boxes this tick ({@code *} touching, {@code !} landed, {@code (immune)} not
 * vulnerable), then the strike log, newest last. Frame-step ({@code 9}) and read it tick by tick.
 */
public class HitboxView implements DebugView {

    private final HitSystem system;

    public HitboxView(HitSystem system) {
        this.system = system;
    }

    @Override public String title() { return "Hitboxes"; }

    @Override
    public void draw() {
        var fighters = system.fighters().entities();
        ImGui.TextUnformatted("fighters: " + fighters.size + "  strikes this tick: " + system.strikes.size);

        if (ImGui.BeginTable("hitboxes", 5, TableFlags.TABLE)) {
            ImGui.TableSetupColumn("Entity", ImGuiTableColumnFlags.WidthStretch);
            ImGui.TableSetupColumn("Team",   ImGuiTableColumnFlags.WidthStretch);
            ImGui.TableSetupColumn("Clip",   ImGuiTableColumnFlags.WidthStretch);
            ImGui.TableSetupColumn("Hurt",   ImGuiTableColumnFlags.WidthStretch);
            ImGui.TableSetupColumn("Hit",    ImGuiTableColumnFlags.WidthStretch);
            ImGui.TableHeadersRow();
            for (int i = 0; i < fighters.size; i++) {
                var e = fighters.get(i);
                var boxes = Hitboxes.get(e);
                ImGui.TableNextRow();
                ImGui.TableNextColumn(); ImGui.TextUnformatted(Name.of(e) + " " + e);
                ImGui.TableNextColumn(); ImGui.TextUnformatted(String.valueOf(boxes.team));
                ImGui.TableNextColumn(); ImGui.TextUnformatted((boxes.clip == null) ? "" : "%s %.2fs".formatted(boxes.clip, boxes.clipTime));
                ImGui.TableNextColumn(); ImGui.TextUnformatted(names(boxes, Hitbox.Kind.HURT) + (boxes.vulnerable ? "" : " (immune)"));
                ImGui.TableNextColumn(); ImGui.TextUnformatted(names(boxes, Hitbox.Kind.HIT));
            }
            ImGui.EndTable();
        }

        if (ImGui.Button(system.logPaused ? "resume log" : "pause log")) {
            system.logPaused = !system.logPaused;
        }
        ImGui.SameLine();
        if (ImGui.Button("clear##strikes")) {
            system.log.clear();
        }
        if (ImGui.BeginChild("strikes", ImTemp.ImVec2_1(0, 160))) {
            for (var line : system.log) {
                ImGui.TextUnformatted(line);
            }
            if (!system.logPaused) {
                ImGui.SetScrollHereY(1f);
            }
        }
        ImGui.EndChild();
    }

    private static String names(Hitboxes boxes, Hitbox.Kind kind) {
        var text = new StringBuilder();
        for (int i = 0; i < boxes.active.size; i++) {
            var slot = boxes.active.get(i);
            if (slot.box.kind() != kind) continue;
            if (!text.isEmpty()) text.append(", ");
            text.append(slot.box.name());
            if (slot.touching)  text.append('*');
            if (slot.connected) text.append('!');
        }
        return text.toString();
    }

    /** Built on first draw: imgui types must not be touched before {@code ImGuiGdx.load()}. */
    private static class TableFlags {
        private static final ImGuiTableFlags TABLE = ImGuiTableFlags.Borders
                .or(ImGuiTableFlags.RowBg).or(ImGuiTableFlags.SizingStretchSame);
    }
}
