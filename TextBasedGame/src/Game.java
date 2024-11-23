import java.util.Scanner;

public class Game {
    
    private static final int MAP_SIZE = 10;
    private static final Position STARTING_POSITION = new Position(3, 5);
    private final Room[] rooms;
    private final String[][] roomFeatures;
    private final Map gameMap;
    private final Score score;
    private boolean[] solvedPuzzles;
    private boolean []roomsVisited;
    private String[] puzzleAnswers; 
    private final Inventory inventory;
    private final Scanner scanner;
    private int[] puzzleOrder = {0, 3, 5, 8}; 
    private Position currentPosition;
    private boolean isRunning;

    public Game() {
        gameMap = new Map(MAP_SIZE, MAP_SIZE);
        score = new Score(0);
        inventory = new Inventory();
        scanner = new Scanner(System.in);
        rooms = new Room[10];
        roomFeatures = new String[10][];
        solvedPuzzles = new boolean[10];
        roomsVisited = new boolean[10];
        puzzleAnswers = new String[10];
        currentPosition = STARTING_POSITION;
        isRunning = true;
        
        initialiseRooms();
        initialiseFeatures();
        initialisePuzzles();
        gameMap.updatePlayerPosition(currentPosition);
    }

    public void gameLoop() {
        displayIntro();
        System.out.println("\nWelcome to Temporal Paradox!");
        System.out.println("You find yourself in the Main Laboratory...");
        System.out.println("\nHere are the available commands:");
        displayHelp();
        
        handleLook(""); 

        while (isRunning) {
            System.out.print("\nEnter command: ");
            String input = scanner.nextLine().toLowerCase().trim();
            processCommand(input);
        }
        scanner.close();
    }


    private void processCommand(String input) {
        if (input.isEmpty()) {
            System.out.println("Please enter a command. Type 'help' for available commands.");
            return;
        }

        String[] parts = input.split(" ", 2);
        String command = parts[0];
        String argument;
        if (parts.length > 1) {
            argument = parts[1];
        } else {
            argument = "";
        }

        switch (command) {
            case "move":
                handleMove(argument);
                break;

            case "look":
                handleLook(argument);
                break;

            case "inventory":
                System.out.println(inventory.displayInventory());
                break;
            
            case "hint":
                displayCurrentObjective();
                break;

            case "score":
                System.out.println("Current score: " + score.getScore());
                break;

            case "collect":
                handleCollectItem(argument);
                break;

            case "map":
                System.out.println(gameMap.displayEnhanced());
                break;

            case "help":
                displayHelp();
                break;

            case "quit":
                handleQuit();
                break;
            default:
                System.out.println("Unknown command. Type 'help' for all of the available commands.");
        }
    }

    private void handleMove(String direction) {
        if (direction.isEmpty()) {
            System.out.println("Please specify a direction.Choose from either (north, south, east, west).");
            return;
        }

        Position newPosition = calculateNewPosition(direction);
        if (newPosition == null) {
            System.out.println("You cannot move in that direction. It's beyond the mansion's boundaries.");
            return;
        }

        Room currentRoom = getRoomAtPosition(currentPosition);
        if (currentRoom != null) {  
            int currentRoomIndex = getRoomIndex(currentRoom);
            if (shouldRequirePuzzle(currentRoomIndex) && !solvedPuzzles[currentRoomIndex]) {
                System.out.println("You need to solve this room's puzzle before leaving!");
                return;
            }
        }

        if (isValidPosition(newPosition)) {
            currentPosition = newPosition;
            gameMap.updatePlayerPosition(currentPosition);
            
            Room targetRoom = getRoomAtPosition(currentPosition);
            if (targetRoom != null) {
                System.out.println("You move " + direction + " to the " + targetRoom.getName());
                handleLook("");
                
                
                int newRoomIndex = getRoomIndex(targetRoom);
                if (!roomsVisited[newRoomIndex]) {
                    roomsVisited[newRoomIndex] = true;
                    score.visitRoom();
                }
                
                
                if (shouldRequirePuzzle(newRoomIndex) && !solvedPuzzles[newRoomIndex]) {
                    presentRoomPuzzle(newRoomIndex);
                }
            } else {
                System.out.println("You move " + direction + " to an empty space in the mansion.");
            }
        } else {
            System.out.println("You cannot move in that direction.");
        }
    }

    private void handleLook(String target) {
        if (target.isEmpty()) {
            displayCurrentRoom();
        } else {
            handleLookAt(target);
        }
    }

    private void handleLookAt(String target) {
        Room currentRoom = getRoomAtPosition(currentPosition);
        if (currentRoom == null) {
            System.out.println("You don't see anything like that here.");
            return;
        }

        int roomIndex = getRoomIndex(currentRoom);
        
        if (roomIndex != -1 && hasFeature(roomIndex, target)) {
            String description = getFeatureDescription(target);
            System.out.println(description);
            
            if (target.equals("broken_keycard") || target.equals("circuits") || 
                target.equals("recipe_book") || target.equals("sundial")) {
                checkForPuzzle(roomIndex, target);
            }
        } else if (inventory.hasItem(target) != -1) {
            System.out.println(getItemDescription(target));
        } else {
            System.out.println("You don't see any " + target + " here.");
        }
    }

    private Position calculateNewPosition(String direction) {
        Position newPosition = new Position(currentPosition.x, currentPosition.y);
        switch (direction) {
            case "north" -> newPosition.y--;
            case "south" -> newPosition.y++;
            case "east" -> newPosition.x++;
            case "west" -> newPosition.x--;
            default -> {
                return null;
            }
        }
        return newPosition;
    }
    private boolean isValidPosition(Position pos) {
        return pos.x >= 0 && pos.x < MAP_SIZE && pos.y >= 0 && pos.y < MAP_SIZE;
    }

    private void initialiseRooms() {
        rooms[0] = new Room("Main Laboratory", "A sophisticated lab filled with mysterious equipment and the damaged Chronosphere", 'M', new Position(5, 5));
        rooms[1] = new Room("Library", "Walls lined with ancient physics books and quantum theory manuscripts", 'L', new Position(3, 3));
        rooms[2] = new Room("Conservatory", "A glass-enclosed room with strange, time-affected plants", 'C', new Position(2, 2));
        rooms[3] = new Room("Workshop", "A cluttered room with tools and half-finished inventions", 'W', new Position(8, 8));
        rooms[4] = new Room("Study", "A cozy room with a desk covered in research notes", 'S', new Position(5, 7));
        rooms[5] = new Room("Kitchen", "An old Victorian kitchen with modern scientific equipment", 'K', new Position(5, 3));
        rooms[6] = new Room("Basement Laboratory", "A darker, more experimental lab space", 'B', new Position(2, 8));
        rooms[7] = new Room("Attic", "A dusty space filled with failed experiments", 'A', new Position(8, 2));
        rooms[8] = new Room("Garden", "An overgrown garden with temporally-shifted flora", 'G', new Position(7, 6));
        rooms[9] = new Room("First Bedroom", "Dr. TTN's personal quarters, frozen in time", 'F', new Position(3, 4));

        for (Room room : rooms) {
            gameMap.placeRoom(room.getPosition(), room.getSymbol());
        }
    }

    private void initialiseFeatures() {
        roomFeatures[0] = new String[]{"chronosphere", "equipment", "research_manual", "broken_keycard"};  
        roomFeatures[1] = new String[]{"research_manual", "desk", "quantum_equations", "old_diary"};               
        roomFeatures[2] = new String[]{"plants", "fountain", "strange_flower", "garden_tools"};          
        roomFeatures[3] = new String[]{"tools", "workbench", "power_core", "circuits"};                 
        roomFeatures[4] = new String[]{"papers", "chair", "blackboard", "time_calculations"};           
        roomFeatures[5] = new String[]{"stove", "ingredients", "recipe_book", "crystal_vial"};          
        roomFeatures[6] = new String[]{"experiments", "tubes", "failed_prototypes", "lab_journal"};     
        roomFeatures[7] = new String[]{"boxes", "window", "old_photographs", "dusty_machine"};          
        roomFeatures[8] = new String[]{"strange_flower", "pond", "sundial", "temporal_anomaly"};              
        roomFeatures[9] = new String[]{"bed", "mirror", "personal_diary", "family_photo"};
    }

    private void initialisePuzzles() {
        puzzleAnswers[0] = "1234"; 
        puzzleAnswers[3] = "POWER"; 
        puzzleAnswers[5] = "THYME"; 
        puzzleAnswers[8] = "12:00"; 

        if (getRoomAtPosition(currentPosition) != null) {
            int roomIndex = getRoomIndex(getRoomAtPosition(currentPosition));
            if (roomIndex == 1) { 
                inventory.addItem("research_manual");
            }
        }
    }

    private void presentKeypadPuzzle() {
        if (inventory.hasItem("broken_keycard") == -1) {
            System.out.println("You need to collect the broken keycard first.");
            return;
        }
        if (inventory.hasItem("research_manual") == -1) {
            System.out.println("You need the research manual to understand the keycard.");
            return;
        }

        System.out.println("\n=== KEYCARD REPAIR PUZZLE ===");
        System.out.println("The broken keycard needs a 4-digit code to be repaired.");
        System.out.println("Your research manual suggests looking for clues in the lab notes.");
        System.out.println("Hint: The research notes mention: 'The year it all began, but backwards.'");
        
        System.out.print("Enter the 4-digit code: ");
        String answer = scanner.nextLine().trim();
        
        if (answer.equals(puzzleAnswers[0])) {
            System.out.println("The keycard hums with energy as it repairs itself!");
            solvedPuzzles[0] = true;
            inventory.removeItem("broken_keycard");
            inventory.addItem("repaired_keycard");
            score.solvePuzzle();
            if (checkWinCondition()) {
                handleWin();
            }
        } else {
            System.out.println("Nothing happens. That wasn't the correct code.");
        }
    }

    private void presentCircuitPuzzle() {
        if (inventory.hasItem("power_core") == -1) {
            System.out.println("You need to collect the power core first.");
            return;
        }
        if (inventory.hasItem("repaired_keycard") == -1) {
            System.out.println("You need the repaired keycard to access the circuits.");
            return;
        }

        System.out.println("\n=== CIRCUIT REPAIR PUZZLE ===");
        System.out.println("The circuit board needs a specific sequence of power routing.");
        System.out.println("Hint: 'P_W_R' - Find the missing letters in the equipment around you.");
        
        System.out.print("Enter the sequence: ");
        String answer = scanner.nextLine().trim().toUpperCase();
        
        if (answer.equals(puzzleAnswers[3])) {
            System.out.println("The circuits light up in sequence! Power is restored!");
            solvedPuzzles[3] = true;
            inventory.removeItem("power_core");
            inventory.addItem("power_module");
            score.solvePuzzle();
        }
        if (checkWinCondition()) {
            handleWin();
        } else {
            System.out.println("The circuits remain dark. That wasn't the correct sequence.");
        }
    }

    private void presentRecipePuzzle() {
        if (inventory.hasItem("crystal_vial") == -1) {
            System.out.println("You need to collect the crystal vial first.");
            return;
        }
        if (inventory.hasItem("power_module") == -1) {
            System.out.println("You need the power module to read the recipe.");
            return;
        }

        System.out.println("\n=== TEMPORAL RECIPE PUZZLE ===");
        System.out.println("The recipe book contains a strange temporal recipe.");
        System.out.println("Hint: 'What herb represents time? It's in the garden...'");
        
        System.out.print("Enter the herb name: ");
        String answer = scanner.nextLine().trim().toUpperCase();
        
        if (answer.equals(puzzleAnswers[5])) {
            System.out.println("The recipe glows with temporal energy!");
            solvedPuzzles[5] = true;
            inventory.removeItem("crystal_vial");
            inventory.addItem("temporal_essence");
            score.solvePuzzle();
        }
        if (checkWinCondition()) {
            handleWin();
        } 
    }

    private void presentSundialPuzzle() {
        if (inventory.hasItem("strange_flower") == -1) {
            System.out.println("You need to collect the strange flower first.");
            return;
        }
        if (inventory.hasItem("temporal_essence") == -1) {
            System.out.println("You need the temporal essence to activate the sundial.");
            return;
        }

        System.out.println("\n=== SUNDIAL PUZZLE ===");
        System.out.println("The sundial seems stuck between times.");
        System.out.println("Hint: 'When does the loop reset?' (Use format: HH:MM)");
        
        System.out.print("Enter the time: ");
        String answer = scanner.nextLine().trim();
        
        if (answer.equals(puzzleAnswers[8])) {
            System.out.println("The sundial aligns perfectly! Time energy flows!");
            solvedPuzzles[8] = true;
            inventory.removeItem("strange_flower");
            inventory.addItem("time_shard");
            score.solvePuzzle();
        }
        if (checkWinCondition()) {
            handleWin();
        } else {
            System.out.println("The sundial remains stuck. That wasn't the correct time.");
        }
    }

    private boolean shouldRequirePuzzle(int roomIndex) {
        return roomIndex == 0 || roomIndex == 3 || roomIndex == 5 || roomIndex == 8;
    }

    private boolean canAttemptPuzzle(int roomIndex) {
        // Find where this room's puzzle is in the sequence
        int puzzlePosition = -1;
        for (int i = 0; i < puzzleOrder.length; i++) {
            if (puzzleOrder[i] == roomIndex) {
                puzzlePosition = i;
                break;
            }
        }

        if (puzzlePosition == -1) return false;
        
        for (int i = 0; i < puzzlePosition; i++) {
            if (!solvedPuzzles[puzzleOrder[i]]) {
                return false;
            }
        }
        
        return true;
    }

    
    private void presentRoomPuzzle(int roomIndex) {
        System.out.println("\nThis room contains a puzzle that needs to be solved!");
        switch(roomIndex) {
            case 0 -> {
                System.out.println("You notice a broken keycard that needs repair...");
                System.out.println("Hint: Look at the 'broken_keycard' to attempt the puzzle.");
            }
            case 3 -> {
                System.out.println("The room's circuits seem to need configuration...");
                System.out.println("Hint: Look at the 'circuits' to attempt the puzzle.");
            }
            case 5 -> {
                System.out.println("A mysterious recipe book catches your attention...");
                System.out.println("Hint: Look at the 'recipe_book' to attempt the puzzle.");
            }
            case 8 -> {
                System.out.println("The sundial seems to be stuck at the wrong time...");
                System.out.println("Hint: Look at the 'sundial' to attempt the puzzle.");
            }
        }
    }

    private void checkForPuzzle(int roomIndex, String feature){
        if (solvedPuzzles[roomIndex]) {
            if (feature.equals("broken_keycard") || feature.equals("circuits") || 
                feature.equals("recipe_book") || feature.equals("sundial")) {
                System.out.println("You've already solved this room's puzzle.");
            }
            return;
        }

        if (!canAttemptPuzzle(roomIndex)) {
            switch (roomIndex) {
                case 0:
                    if (inventory.hasItem("research_manual") == -1) {
                        System.out.println("\nYou need to collect the research manual from the Library first.");
                        System.out.println("Use 'collect research_manual' when in the Library.");
                    }
                    break;
                case 3:
                    if (inventory.hasItem("repaired_keycard") == -1) {
                        System.out.println("\nYou need the repaired keycard from the Main Laboratory first.");
                    }
                    break;
                case 5:
                    if (inventory.hasItem("power_module") == -1) {
                        System.out.println("\nYou need the power module from the Workshop first.");
                    }
                    break;
                case 8:
                    if (inventory.hasItem("temporal_essence") == -1) {
                        System.out.println("\nYou need the temporal essence from the Kitchen first.");
                    }
                    break;
            }
            return;
        }

      
        switch (roomIndex) {
            case 0: 
                if (feature.equals("broken_keycard")) {
                    if (inventory.hasItem("research_manual") != -1) {
                        if (inventory.hasItem("broken_keycard") != -1) {
                            System.out.println("\nUsing the research manual, you begin to understand the keycard mechanism.");
                            presentKeypadPuzzle();
                        } else {
                            System.out.println("\nYou need to collect the broken keycard first. Use 'collect broken_keycard'");
                        }
                    } else {
                        System.out.println("\nThe keycard is complex. You need the research manual from the Library first.");
                    }
                }
                break;

            case 3: 
                if (feature.equals("circuits")) {
                    if (inventory.hasItem("repaired_keycard") != -1) {
                        if (inventory.hasItem("power_core") != -1) {
                            System.out.println("\nYou use the repaired keycard to access the circuit controls.");
                            presentCircuitPuzzle();
                        } else {
                            System.out.println("\nYou need to collect the power core first. Use 'collect power_core'");
                        }
                    } else {
                        System.out.println("\nYou need to repair the keycard from the Main Laboratory first.");
                    }
                }
                break;

            case 5: 
                if (feature.equals("recipe_book")) {
                    if (inventory.hasItem("power_module") != -1) {
                        if (inventory.hasItem("crystal_vial") != -1) {
                            System.out.println("\nWith power restored, you can clearly read the temporal recipe.");
                            presentRecipePuzzle();
                        } else {
                            System.out.println("\nYou need to collect the crystal vial first. Use 'collect crystal_vial'");
                        }
                    } else {
                        System.out.println("\nYou need to restore power from the Workshop first.");
                    }
                }
                break;

            case 8: 
                if (feature.equals("sundial")) {
                    if (inventory.hasItem("temporal_essence") != -1) {
                        if (inventory.hasItem("strange_flower") != -1) {
                            System.out.println("\nYou apply the temporal essence to the sundial, making it responsive.");
                            presentSundialPuzzle();
                        } else {
                            System.out.println("\nYou need to collect the strange flower first. Use 'collect strange_flower'");
                        }
                    } else {
                        System.out.println("\nYou need the temporal essence from the Kitchen first.");
                    }
                }
                break;
        }
    }

    

    private void displayCurrentRoom() {
        Room currentRoom = getRoomAtPosition(currentPosition);
        if (currentRoom != null) {
            System.out.println("\nYou are in the " + currentRoom.getName());
            System.out.println(currentRoom.getDescription());
            
            int roomIndex = getRoomIndex(currentRoom);
            if (shouldRequirePuzzle(roomIndex)) {
                if (solvedPuzzles[roomIndex]) {
                    System.out.println("You have solved this room's puzzle.");
                } else {
                    System.out.println("This room contains an unsolved puzzle!");
                }
            }
            
            int solvedCount = 0;
            for (boolean solved : solvedPuzzles) {
                if (solved) solvedCount++;
            }
            System.out.println("\nPuzzles solved: " + solvedCount + "/4");
            
            displayRoomFeatures(currentRoom);
        } else {
            System.out.println("You are in an empty space in the mansion.");
        }
    }

    private void displayRoomFeatures(Room room) {
        int roomIndex = getRoomIndex(room);
        if (roomIndex != -1 && roomFeatures[roomIndex] != null) {
            System.out.println("\nYou can see:");
            for (String feature : roomFeatures[roomIndex]) {
                System.out.println("- " + feature);
            }
        }
    }

    private boolean checkWinCondition() {
        // Count completed puzzles
        int completedPuzzles = 0;
        for (boolean solved : solvedPuzzles) {
            if (solved) {
                completedPuzzles++;
            }
        }
        
        boolean hasAllItems = inventory.hasItem("repaired_keycard") != -1 &&
                            inventory.hasItem("power_module") != -1 &&
                            inventory.hasItem("temporal_essence") != -1 &&
                            inventory.hasItem("time_shard") != -1;
        
        return completedPuzzles == 4 && hasAllItems;
    }

    private void handleWin() {
        System.out.println("""
            
            =====================================================
            CONGRATULATIONS! You've solved all temporal puzzles!
            -----------------------------------------------------
            With all four temporal artifacts in your possession:
            - The repaired keycard
            - The power module
            - The temporal essence
            - The time shard
            
            You combine their energies with the Chronosphere...
            
            There's a brilliant flash of light, and you feel the
            time loop finally breaking! Dr. TTN is free at last!
            -----------------------------------------------------
            Final Score: """ + score.getScore() + """
            
            Thank you for playing Temporal Paradox!
            =====================================================
            """);
        
        isRunning = false;
    }

    private Room getRoomAtPosition(Position pos) {
        for (Room room : rooms) {
            Position roomPos = room.getPosition();
            if (roomPos.x == pos.x && roomPos.y == pos.y) {
                return room;
            }
        }
        return null;
    }

    private int getRoomIndex(Room room) {
        for (int i = 0; i < rooms.length; i++) {
            if (rooms[i] == room) {
                return i;
            }
        }
        return -1;
    }

    private boolean hasFeature(int roomIndex, String feature) {
        if (roomFeatures[roomIndex] == null) return false;
        for (String f : roomFeatures[roomIndex]) {
            if (f.equals(feature)) {
                return true;
            }
        }
        return false;
    }

    

    private void handleCollectItem(String item) {
        Room currentRoom = getRoomAtPosition(currentPosition);
        if (currentRoom == null) {
            System.out.println("There's nothing to collect here.");
            return;
        }

        int roomIndex = getRoomIndex(currentRoom);
        String[] features = roomFeatures[roomIndex];

        for (String feature : features) {
            if (feature.equals(item)) {
                if (isCollectible(item)) {
                    inventory.addItem(item);
                    System.out.println("You collected: " + item);
                    return;
                }
            }
        }
        System.out.println("You can't collect that.");
    }

    private boolean isCollectible(String item) {
        switch (item) {
            case "research_manual":    
            case "broken_keycard":     
            case "repaired_keycard":   
            case "power_module":       
            case "temporal_essence":   
            case "time_shard":         
            case "power_core":         
            case "crystal_vial":       
            case "strange_flower":     
                return true;
            default:
                return false;
            }
        
    };
    
   
    

    private String getFeatureDescription(String feature) {
        return switch (feature) {
            case "chronosphere" -> "The Chronosphere is a complex device with swirling temporal energies. It appears damaged.";
            case "broken_keycard" -> "A damaged keycard that might be repairable with the right code.";
            case "research_manual" -> "Notes mentioning experiments beginning in '4321'... that's odd.";
            case "circuits" -> "A complex circuit board with missing connections. Letters P_W_R are visible.";
            case "recipe_book" -> "A mysterious recipe book. One page talks about a temporal herb.";
            case "sundial" -> "An unusual sundial that seems to be stuck. It feels important to the time loop.";
            case "power_core" -> "A sophisticated power source. It needs proper circuit connectivity.";
            case "strange_flower" -> "A flower that seems to age and rejuvenate repeatedly.";
            case "quantum_equations" -> "Complex equations about temporal mechanics. Some numbers stand out.";
            case "crystal_vial" -> "A vial that seems to manipulate time around it.";
            // ... (add more feature descriptions)
            default -> "A rather ordinary " + feature + ".";
        };
    }

    private String getItemDescription(String item) {
        return "A " + item + " - no special description available.";
    }

    private void handleQuit() {
        System.out.println("Are you sure you want to quit? (yes/no)");
        String response = scanner.nextLine().toLowerCase().trim();
        if (response.equals("yes")) {
            System.out.println("Thanks for playing! Final score: " + score.getScore());
            isRunning = false;
        }
    }

    private void displayCurrentObjective() {
        System.out.println("\n=== CURRENT OBJECTIVE ===");
        
        // Check for research manual first
        if (inventory.hasItem("research_manual") == -1) {
            System.out.println("Go to the Library and collect the research manual ('collect research_manual')");
            return;
        }
       

        if (!solvedPuzzles[0]) {
            if (inventory.hasItem("broken_keycard") == -1) {
                System.out.println("Now that you have the research manual, go to the Main Laboratory and collect the broken keycard ('collect broken_keycard')");
            } else {
                System.out.println("Use the research manual to repair the broken keycard (look at 'broken_keycard')");
            }
            return;
        }

    
        if (!solvedPuzzles[3]) {
            if (inventory.hasItem("power_core") == -1) {
                System.out.println("Go to the Workshop and collect the power core ('collect power_core')");
            } else {
                System.out.println("Use the repaired keycard to fix the circuits in the Workshop (look at 'circuits')");
            }
            return;
        }

        
        if (!solvedPuzzles[5]) {
            if (inventory.hasItem("crystal_vial") == -1) {
                System.out.println("Go to the Kitchen and collect the crystal vial ('collect crystal_vial')");
            } else {
                System.out.println("Use the power module to decode the recipe book (look at 'recipe_book')");
            }
            return;
        }

        if (!solvedPuzzles[8]) {
            if (inventory.hasItem("strange_flower") == -1) {
                System.out.println("Go to the Garden and collect the strange flower ('collect strange_flower')");
            } else {
                System.out.println("Use the temporal essence to align the sundial (look at 'sundial')");
            }
            return;
        }

        if (checkWinCondition()) {
            System.out.println("Congratulations! You have all the items needed to break free from the time loop!");
            System.out.println("Your collection of temporal artifacts is complete.");
        } else {
            System.out.println("Check your inventory and make sure you have all required items.");
        }
    }

    private void displayHelp() {
        System.out.println("""
             ================ GAME COMMANDS ================
            - move <direction>  : Move in specified direction (north, south, east, west)
            - look             : Look around your current room
            - look <feature>   : Look at a specific feature in the room
            - collect <item>    : Pick up an item from the current room
            - look <item>      : Look at a specific item in your inventory
            - inventory        : Display your inventory
            - hint             : Get a hint about your current objective
            - score           : Display your current score
            - map             : Display the game map
            - help            : Display this help message
            - quit            : Exit the game
            ===============================================
            """);   
    }

    private void displayIntro() {
        System.out.println("""
         _______  _______  __   __  _______  _______  ______    _______  ___       
        |       ||       ||  |_|  ||       ||       ||    _ |  |   _   ||   |      
        |_     _||    ___||       ||    _  ||   _   ||   | ||  |  |_|  ||   |      
          |   |  |   |___ |       ||   |_| ||  | |  ||   |_||_ |       ||   |      
          |   |  |    ___||       ||    ___||  |_|  ||    __  ||       ||   |___   
          |   |  |   |___ | ||_|| ||   |    |       ||   |  | ||   _   ||       |  
          |___|  |_______||_|   |_||___|    |_______||___|  |_||__| |__||_______|  
                _______  _______  ______    _______  ______   _______  __   __            
                |       ||   _   ||    _ |  |   _   ||      | |       ||  |_|  |           
                |    _  ||  |_|  ||   | ||  |  |_|  ||  _    ||   _   ||       |           
                |   |_| ||       ||   |_||_ |       || | |   ||  | |  ||       |           
                |    ___||       ||    __  ||       || |_|   ||  |_|  | |     |            
                |   |    |   _   ||   |  | ||   _   ||       ||       ||   _   |           
                |___|    |__| |__||___|  |_||__| |__||______| |_______||__| |__|  
                
            You are  Dr.TTN , a brilliant quantum physicist who was working
            on a revolutionary time manipulation device called the Chronosphere in his
            private laboratory. During a crucial experiment, something went terribly 
            wrong, causing a temporal explosion that trapped her in a 60-minute time 
            loop within her Victorian-era mansion-turned-laboratory. Each loop resets at
            midnight, but anything in her special "quantum-locked" inventory remains with
            her across loops.
            """);
    }

    public static void main(String[] args) {
        Game game = new Game();
        game.gameLoop();
    }
}