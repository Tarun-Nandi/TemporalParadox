public class Inventory {
    private final int MAX_ITEMS = 10;
    private String [] inventory;
    private int currentSize;

    public Inventory(){
        this.inventory = new String[MAX_ITEMS];
        this.currentSize = 0;
    }
    public void addItem(String item){
        if (currentSize >= MAX_ITEMS){
            System.out.println("Your inventory is full.Try remove some items if you need this current item!");
        }
        else{
            inventory[currentSize] = item;
            currentSize ++;
        }

    }
    public int hasItem(String item){
        for (int i = 0; i < MAX_ITEMS; i++){
            if (item.equals(inventory[i])){
                return i;

            }
        }
        return -1;

    }
    public void removeItem(String item){
        int position = hasItem(item);
        if (position == -1){
            return;
        }
        for(int i = position;  i < currentSize -1 ;i++){
            inventory[i] = inventory[i + 1];
        }
        inventory[currentSize -1 ] = null;
        currentSize--;


    }

    public String displayInventory() {
        if (currentSize == 0) {
            return "";
        }
        String display = "Inventory contents:\n";
        for (int i = 0; i < currentSize; i++) {
            display += inventory[i];
            if (i < currentSize - 1) {
                display += "\n";
            }
        }
        
        return display;
    }
}
