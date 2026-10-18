package lando.systems.ld60.ui;

import com.zendogames.zenlib.ecs.Family;
import com.zendogames.zenlib.ecs.World;
import com.zendogames.zenlib.ui.DebugView;
import imgui.ImGui;
import imgui.enums.ImGuiTableColumnFlags;
import imgui.enums.ImGuiTableFlags;
import lando.systems.ld60.game.components.Emitter;
import lando.systems.ld60.game.systems.ParticleSystem;

public final class GameDebugViews {

    public static DebugView particlePool(World world) {
        var system = world.getSystem(ParticleSystem.class);
        var emitters = world.family(Family.all(Emitter.TYPE));
        return new DebugView() {
            @Override public String title()          { return "ParticlePool"; }
            @Override public boolean openByDefault() { return false; }
            @Override public void draw() {
                ImGui.TextUnformatted("emitters: " + emitters.entities().size + "  particles: " + system.activeParticles.size());

                var pool = system.pool;
                if (ImGui.BeginTable("particle-pool", 4, TableFlags.TABLE)) {
                    ImGui.TableSetupColumn("Type",   ImGuiTableColumnFlags.WidthStretch);
                    ImGui.TableSetupColumn("Active", ImGuiTableColumnFlags.WidthStretch);
                    ImGui.TableSetupColumn("Peak",   ImGuiTableColumnFlags.WidthStretch);
                    ImGui.TableSetupColumn("Free",   ImGuiTableColumnFlags.WidthStretch);
                    ImGui.TableHeadersRow();
                    ImGui.TableNextRow();
                    ImGui.TableNextColumn(); ImGui.TextUnformatted("ParticleData");
                    ImGui.TableNextColumn(); ImGui.TextUnformatted(String.valueOf(system.activeData.size()));
                    ImGui.TableNextColumn(); ImGui.TextUnformatted(String.valueOf(pool.peak));
                    ImGui.TableNextColumn(); ImGui.TextUnformatted(String.valueOf(pool.getFree()));
                    ImGui.EndTable();
                }
            }
        };
    }

    private static class TableFlags {
        private static final ImGuiTableFlags TABLE = ImGuiTableFlags.Borders
                .or(ImGuiTableFlags.RowBg).or(ImGuiTableFlags.SizingStretchSame);
    }
}
