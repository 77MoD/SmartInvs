package fr.minuskube.inv.content;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface InventoryContents {

    SmartInventory inventory();
    Pagination pagination();

    Optional<SlotIterator> iterator(String id);

    SlotIterator newIterator(String id, SlotIterator.Type type, int startRow, int startColumn);
    SlotIterator newIterator(SlotIterator.Type type, int startRow, int startColumn);


    ClickableItem[] all();

    Optional<Integer> firstEmpty();

    Optional<ClickableItem> get(int row, int column);
    Optional<ClickableItem> get(int slot);

    InventoryContents set(int row, int column, ClickableItem item);
    InventoryContents set(int slot, ClickableItem item);

    InventoryContents add(ClickableItem item);

    InventoryContents fill(ClickableItem item);
    InventoryContents fillRow(int row, ClickableItem item);
    InventoryContents fillColumn(int column, ClickableItem item);
    InventoryContents fillBorders(ClickableItem item);

    InventoryContents fillRect(int fromRow, int fromColumn,
                               int toRow, int toColumn, ClickableItem item);

    <T> T property(String name);
    <T> T property(String name, T def);

    InventoryContents setProperty(String name, Object value);

    class Impl implements InventoryContents {

        private final SmartInventory inv;
        private final UUID player;

        private final ClickableItem[] contents;

        private final Pagination pagination = new Pagination.Impl();
        private final Map<String, SlotIterator> iterators = new HashMap<>();
        private final Map<String, Object> properties = new HashMap<>();

        public Impl(SmartInventory inv, UUID player) {
            this.inv = inv;
            this.player = player;
            this.contents = new ClickableItem[inv.getColumns()*inv.getRows()];
        }

        @Override
        public SmartInventory inventory() { return inv; }

        @Override
        public Pagination pagination() { return pagination; }

        @Override
        public Optional<SlotIterator> iterator(String id) {
            return Optional.ofNullable(this.iterators.get(id));
        }

        @Override
        public SlotIterator newIterator(String id, SlotIterator.Type type, int startRow, int startColumn) {
            SlotIterator iterator = new SlotIterator.Impl(this, inv,
                    type, startRow, startColumn);

            this.iterators.put(id, iterator);
            return iterator;
        }



        @Override
        public SlotIterator newIterator(SlotIterator.Type type, int startRow, int startColumn) {
            return new SlotIterator.Impl(this, inv, type, startRow, startColumn);
        }


        @Override
        public ClickableItem[] all() { return contents; }

        @Override
        public Optional<Integer> firstEmpty() {
            for (int index = 0; index < contents.length; index++) {
                    if(!this.get(index).isPresent())
                        return Optional.of(index);
            }

            return Optional.empty();
        }

        @Override
        public Optional<ClickableItem> get(int row, int column) {
            return get(getSlot(row, column));
        }

        @Override
        public Optional<ClickableItem> get(int slot) {
            if(!isInRange(slot))
                return Optional.empty();

            return Optional.ofNullable(contents[slot]);
        }
        private boolean isInRange(int row, int column){
            return isInRange(getSlot(row, column));
        }
        private int getSlot(int row, int column){
            return row*inv.getColumns()+column;
        }
        private boolean isInRange(int slot){
            return slot < contents.length && slot >= 0;
        }

        @Override
        public InventoryContents set(int row, int column, ClickableItem item) {
            return set(getSlot(row, column), item);
        }

        @Override
        public InventoryContents set(int slot, ClickableItem item) {
            if(!isInRange(slot))
                return this;
            contents[slot] = item;
            update(slot, item != null ? item.getItem() : null);
            return this;
        }

        @Override
        public InventoryContents add(ClickableItem item) {
            for(int index = 0; index < contents.length; index++) {
                    if(contents[index] == null) {
                        set(index, item);
                        return this;
                    }
            }

            return this;
        }

        @Override
        public InventoryContents fill(ClickableItem item) {
            for(int index = 0; index < contents.length; index++)
                    set(index, item);
            return this;
        }

        @Override
        public InventoryContents fillRow(int row, ClickableItem item) {
            if (row < 0 || row >= 6) {
                return this;
            }

            int startIndex = row * 9;
            int endIndex = startIndex + 9;
            for (int index = startIndex; index < endIndex; index++) {
                set(index, item);
            }
            return this;
        }
        @Override
        public InventoryContents fillColumn(int column, ClickableItem item) {
            for(int row = 0; row < contents.length; row++)
                set(row, column, item);

            return this;
        }

        @Override
        public InventoryContents fillBorders(ClickableItem item) {
            fillRect(0, 0, inv.getRows() - 1, inv.getColumns() - 1, item);
            return this;
        }

        @Override
        public InventoryContents fillRect(int fromRow, int fromColumn, int toRow, int toColumn, ClickableItem item) {
            for(int row = fromRow; row <= toRow; row++) {
                for(int column = fromColumn; column <= toColumn; column++) {
                    if(row != fromRow && row != toRow && column != fromColumn && column != toColumn)
                        continue;

                    set(row, column, item);
                }
            }

            return this;
        }


        @SuppressWarnings("unchecked")
        @Override
        public <T> T property(String name) {
            return (T) properties.get(name);
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> T property(String name, T def) {
            return properties.containsKey(name) ? (T) properties.get(name) : def;
        }

        @Override
        public InventoryContents setProperty(String name, Object value) {
            properties.put(name, value);
            return this;
        }


        private void update(int slot, ItemStack item) {
            Player currentPlayer = Bukkit.getPlayer(player);
            if(!inv.getManager().getOpenedPlayers(inv).contains(currentPlayer))
                return;

            Inventory topInventory = currentPlayer.getOpenInventory().getTopInventory();
            topInventory.setItem(slot, item);
        }
    }

}