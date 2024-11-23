public class Map {
    private char[][] map;
    private int width;
    private int height;
    private final char EMPTY = '.';
    private final char PLAYER = '@';
    private Position playerPos;

    public Map(int width, int height) {
        this.width = width;
        this.height = height;
        map = new char[height][width]; 

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                map[y][x] = EMPTY;
            }
        }
    }

    public void placeRoom(Position pos, char symbol) {
        if (pos.x >= 0 && pos.x < width && pos.y >= 0 && pos.y < height) {
            map[pos.y][pos.x] = symbol; 
        }
    }

    public void updatePlayerPosition(Position pos) {
        this.playerPos = pos;
    }


    public String display(){
        String mapDisplay = "";
    for (int y = 0; y < height; y++) {
        for (int x = 0; x < width; x++) {
            mapDisplay += map[y][x];
        }
        mapDisplay += "\n";
    }
    return mapDisplay;
    }

    public String displayEnhanced() {
        String mapDisplay = "";
        mapDisplay += "    "; 
        for (int x = 0; x < width; x++) {
            mapDisplay += x + " ";
        }
        mapDisplay += "\n";
        mapDisplay += "   +" + "-".repeat(width * 2) + "+\n";
        
        for (int y = 0; y < height; y++) {
            mapDisplay += String.format("%2d |", y);
            
            for (int x = 0; x < width; x++) {
                if (playerPos != null && x == playerPos.x && y == playerPos.y) {
                    if (map[y][x] != EMPTY) {
                        mapDisplay += PLAYER + "*"; 
                    } else {
                        mapDisplay += PLAYER + " ";
                    }
                } else {
                    mapDisplay += map[y][x] + " ";
                }
            }
            mapDisplay += "|\n";
        }
        mapDisplay += "   +" + "-".repeat(width * 2) + "+\n";
        
        mapDisplay += "\nGuide\n";
        mapDisplay += "@ = You are here\n";
        mapDisplay += "@* = You are in a room\n";
        mapDisplay += ". = Empty space\n";
        mapDisplay += "M = Main Laboratory\n";
        mapDisplay += "L = Library\n";
        mapDisplay += "C = Conservatory\n";
        mapDisplay += "W = Workshop\n";
        mapDisplay += "S = Study\n";
        mapDisplay += "K = Kitchen\n";
        mapDisplay += "B = Basement Laboratory\n";
        mapDisplay += "A = Attic\n";
        mapDisplay += "G = Garden\n";
        mapDisplay += "F = First Bedroom";
        
        return mapDisplay;
    }

    
}