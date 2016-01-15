package chanceCubes.blocks;

import java.util.Random;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;
import chanceCubes.items.CCubesItems;
import chanceCubes.registry.GiantCubeRegistry;
import chanceCubes.tileentities.TileGiantCube;
import chanceCubes.util.GiantCubeUtil;

public class BlockGiantCube extends BaseChanceBlock implements ITileEntityProvider
{
	public BlockGiantCube()
	{
		super("giant_Chance_Cube");
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta)
	{
		return new TileGiantCube();
	}

	@Override
	public int quantityDropped(Random rand)
	{
		return 0;
	}

	@Override
	public boolean removedByPlayer(World world, BlockPos pos, EntityPlayer player, boolean willHarvest)
	{
		if(!world.isRemote && player != null && !(player instanceof FakePlayer))
		{
			TileGiantCube te = (TileGiantCube) world.getTileEntity(pos);

			if(player.inventory.getCurrentItem() != null && player.inventory.getCurrentItem().getItem().equals(CCubesItems.silkPendant))
			{
				CCubesBlocks.chanceCompactGiantCube.dropBlockAsItem(world, pos, CCubesBlocks.chanceCompactGiantCube.getDefaultState(), 1);
				GiantCubeUtil.removeStructure(te.getMasterPostion(), world);
				return true;
			}

			if(te != null)
			{
				if(!te.hasMaster())
				{
					world.setBlockToAir(pos);
					return false;
				}
				player.addChatMessage(new ChatComponentText("The Giant Cube and rewards are currently In developement"));
				player.addChatMessage(new ChatComponentText("Please let me know what you think of the idea and leave sugestions!"));
				GiantCubeRegistry.INSTANCE.triggerRandomReward(world, te.getMasterPostion(), player, 0);
				GiantCubeUtil.removeStructure(te.getMasterPostion(), world);
			}
		}
		return true;
	}
}