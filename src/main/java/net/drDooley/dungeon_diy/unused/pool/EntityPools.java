package net.drDooley.dungeon_diy.unused.pool;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;

public class EntityPools {
    private ArrayList<Pool> pools;
    final int MIN_VALUE = 0;
    final int MAX_VALUE = 64;
    private class Pool {
        int minRolls = 1;
        int maxRolls = 1;
        int bonusRolls = 0;
        ArrayList<PoolItem> entries = new ArrayList<>();
        public Pool(int minRolls, int maxRolls, int bonusRolls) {
            this.minRolls = Math.max(MIN_VALUE, minRolls);
            this.maxRolls = Math.min(MAX_VALUE, maxRolls);
            this.bonusRolls = Math.max(MIN_VALUE, Math.min(MAX_VALUE, bonusRolls));
        }

        private class PoolItem {
            ItemStack itemStack = ItemStack.EMPTY;
            int weight = 1;
            int minCount = 1;
            int maxCount = 1;
            public PoolItem(ItemStack stack, int weight, int minCount, int maxCount) {
                this.itemStack = stack;
                this.weight = weight;
                this.minCount = Math.max(MIN_VALUE, minCount);
                this.maxCount = Math.min(MAX_VALUE, maxCount);
            }
        }
    }

    public ArrayList<Pool> getPools() { return pools; }

    public int getNumPools() { return pools.size(); }

    public void addPool(int minRolls, int maxRolls, int bonusRolls) {
        pools.add(new Pool(minRolls, maxRolls, bonusRolls));
    }

    public void rmPool(int poolIndex) {
        pools.remove(poolIndex);
    }

    public void addEntry(int poolIndex, int slotIndex, ItemStack stack, int weight, int minCount, int maxCount) {
        Pool pool = pools.get(poolIndex);
        Pool.PoolItem insert = pool.new PoolItem(stack, weight, minCount, maxCount);
        pool.entries.set(slotIndex, insert);
    }

    public void rmEntry(int poolIndex, int entryIndex) {
        pools.get(poolIndex).entries.remove(entryIndex);
    }

    public void copyFrom(EntityPools source) {
        this.pools = source.pools;
    }

    public void saveNBTData(CompoundTag pTag) {
        ListTag poolsTag = new ListTag();
        for (Pool pool : pools) {
            CompoundTag poolTag = new CompoundTag();
            poolTag.putInt("pool.minRolls", pool.minRolls);
            poolTag.putInt("pool.maxRolls", pool.maxRolls);
            poolTag.putInt("pool.bonusRolls", pool.bonusRolls);
            ListTag poolEntriesTag = new ListTag();
            for (int j = 0; j < pool.entries.size(); j++) {
                Pool.PoolItem poolItem = pool.entries.get(j);
                if (poolItem != null) {
                    CompoundTag poolItemTag = new CompoundTag();
                    poolItemTag.putByte("poolItem.slot", (byte) j);
                    poolItemTag.putInt("poolItem.weight", poolItem.weight);
                    poolItemTag.putInt("poolItem.minCount", poolItem.minCount);
                    poolItemTag.putInt("poolItem.maxCount", poolItem.maxCount);
                    poolItem.itemStack.save(poolItemTag);
                    poolEntriesTag.add(poolItemTag);
                }
            }
            poolTag.put("pool.entries", poolEntriesTag);
            poolsTag.add(poolTag);
        }
        pTag.put("pools", poolsTag);
    }

    public void loadNBTData(CompoundTag pTag) {
        pools = new ArrayList<>();
        ListTag poolsTag = pTag.getList("pools", ListTag.TAG_COMPOUND);
        for (int i = 0; i < poolsTag.size(); i++) {
            CompoundTag poolTag = poolsTag.getCompound(i);
            int minRolls = poolTag.getInt("pool.minRolls");
            int maxRolls = poolTag.getInt("pool.maxRolls");
            int bonusRolls = poolTag.getInt("pool.bonusRolls");
            Pool pool = new Pool(minRolls, maxRolls, bonusRolls);
            ListTag poolEntriesTag = poolTag.getList("pool.entries", ListTag.TAG_COMPOUND);
            for (int j = 0; j < poolEntriesTag.size(); j++) {
                CompoundTag poolItemTag = poolEntriesTag.getCompound(j);
                int slot = poolItemTag.getByte("poolItem.slot") & 255;
                int weight = poolItemTag.getInt("poolItem.weight");
                int minCount = poolItemTag.getInt("poolItem.minCount");
                int maxCount = poolItemTag.getInt("poolItem.maxCount");
                ItemStack stack = ItemStack.of(poolItemTag);
                Pool.PoolItem poolItem = pool.new PoolItem(stack, weight, minCount, maxCount);
                pool.entries.set(j, poolItem);
            }
            pools.add(pool);
        }
    }
}
