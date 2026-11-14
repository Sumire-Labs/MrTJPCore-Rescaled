package mrtjp.core.compat.jei

import java.awt.Rectangle
import java.util.List
import mezz.jei.api.{IModPlugin, IModRegistry, JEIPlugin}
import mezz.jei.api.gui.IAdvancedGuiHandler
import mrtjp.core.gui.NodeGui
import net.minecraftforge.fml.relauncher.{Side, SideOnly}

@JEIPlugin
@SideOnly(Side.CLIENT)
class MrTJPCoreJeiPlugin extends IModPlugin {
    override def register(registry:IModRegistry):Unit =
        registry.addAdvancedGuiHandlers(new IAdvancedGuiHandler[NodeGui] {
            override def getGuiContainerClass:Class[NodeGui] = classOf[NodeGui]
            override def getGuiExtraAreas(gui:NodeGui):List[Rectangle] = gui.getExtraGuiAreas
        })
}
