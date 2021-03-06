package chanceCubes.items;

import chanceCubes.blocks.CCubesBlocks;
import chanceCubes.client.ClientProxy;
import chanceCubes.registry.global.GlobalCCRewardRegistry;
import chanceCubes.rewards.IChanceCubeReward;
import chanceCubes.tileentities.TileGiantCube;
import chanceCubes.util.GiantCubeUtil;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

public class ItemSingleUseRewardSelectorPendant extends BaseChanceCubesItem
{

	public ItemSingleUseRewardSelectorPendant()
	{
		super((new Item.Properties()).maxStackSize(1), "single_use_reward_selector_pendant");
		super.addLore("Right click a Chance Cube to summon the reward.");
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand)
	{
		player.setActiveHand(hand);
		ItemStack stack = player.getHeldItem(hand);
		if(player.isShiftKeyDown() && world.isRemote && player.isCreative())
		{
			DistExecutor.runWhenOn(Dist.CLIENT, () -> () ->
			{
				ClientProxy.openRewardSelectorGUI(player, stack);
			});
		}
		return new ActionResult<>(ActionResultType.SUCCESS, stack);
	}

	@Override
	public ActionResultType onItemUse(ItemUseContext context)
	{
		if(context.getPlayer() == null || context.getPlayer().isShiftKeyDown())
			return ActionResultType.FAIL;
		if(context.getWorld().isRemote)
			return ActionResultType.PASS;
		if(context.getItem().getTag() != null && context.getItem().getTag().contains("Reward"))
		{
			PlayerEntity player = context.getPlayer();
			if(player != null)
			{
				if(context.getWorld().getBlockState(context.getPos()).getBlock().equals(CCubesBlocks.CHANCE_CUBE))
				{
					context.getWorld().setBlockState(context.getPos(), Blocks.AIR.getDefaultState());
					IChanceCubeReward reward = GlobalCCRewardRegistry.DEFAULT.getRewardByName(context.getItem().getTag().getString("Reward"));
					if(reward != null)
					{
						GlobalCCRewardRegistry.DEFAULT.triggerReward(reward, context.getWorld(), context.getPos(), context.getPlayer());
						player.setItemStackToSlot(EquipmentSlotType.MAINHAND, ItemStack.EMPTY);
					}
					else
					{
						player.sendMessage(new StringTextComponent("That reward does not exist for this cube!"));

					}
				}
				else if(context.getWorld().getBlockState(context.getPos()).getBlock().equals(CCubesBlocks.GIANT_CUBE))
				{
					TileEntity ent = context.getWorld().getTileEntity(context.getPos());
					if(!(ent instanceof TileGiantCube))
						return ActionResultType.FAIL;
					TileGiantCube giant = (TileGiantCube) ent;
					IChanceCubeReward reward = GlobalCCRewardRegistry.GIANT.getRewardByName(context.getItem().getTag().getString("Reward"));
					if(reward != null)
					{
						GlobalCCRewardRegistry.GIANT.triggerReward(reward, context.getWorld(), giant.getMasterPostion(), context.getPlayer());
						GiantCubeUtil.removeStructure(giant.getMasterPostion(), context.getWorld());
						player.setItemStackToSlot(EquipmentSlotType.MAINHAND, ItemStack.EMPTY);
					}
					else
					{
						player.sendMessage(new StringTextComponent("That reward does not exist for this cube!"));
					}
				}
			}
		}
		return ActionResultType.SUCCESS;
	}
}