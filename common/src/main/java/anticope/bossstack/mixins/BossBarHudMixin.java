package anticope.bossstack.mixins;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.WeakHashMap;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.client.gui.hud.BossBarHud;
import net.minecraft.client.gui.hud.ClientBossBar;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

@Mixin(BossBarHud.class)
public class BossBarHudMixin {
    private static final WeakHashMap<ClientBossBar, Integer> barMap = new WeakHashMap<>();

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Ljava/util/Collection;iterator()Ljava/util/Iterator;"))
    public Iterator<ClientBossBar> onRender(Collection<ClientBossBar> collection) {
        HashMap<String, ClientBossBar> chosenBarMap = new HashMap<>();
        collection.iterator().forEachRemaining(bar -> {
            String name = bar.getName().getString();
            if (chosenBarMap.containsKey(name)) {
                barMap.compute(chosenBarMap.get(name), (clientBossBar, integer) -> (integer == null) ? 2 : integer + 1);
            } else {
                chosenBarMap.put(name, bar);
            }
        });
        return chosenBarMap.values().iterator();
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/ClientBossBar;getName()Lnet/minecraft/text/Text;"))
    public Text onAsFormattedString(ClientBossBar clientBossBar) {
        var count = barMap.get(clientBossBar);
        if (count == null) return clientBossBar.getName();
        barMap.remove(clientBossBar);
        var name = clientBossBar.getName().copy();
        var countText = Text.of(String.format(" x%d", count)).copy();
        countText.setStyle(countText
            .getStyle()
            .withColor(Formatting.GRAY)
            .withItalic(true));
        name.append(countText);
        return name;
    }
}
