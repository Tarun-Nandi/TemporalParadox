    public class Score {
        private final int PUZZLE_VALUE = 10;
        private int startingScore;
        private int roomsVisited;
        private int puzzlesSolved;

        public Score(int startingScore){
            this.startingScore = 0;
            this.roomsVisited = 0;
            this.puzzlesSolved = 0;

        }
        public void visitRoom(){
            roomsVisited ++;


        }

        public void solvePuzzle(){
            puzzlesSolved++;

        }
        
        public double getScore(){
            return startingScore + (puzzlesSolved * PUZZLE_VALUE);

        }
    }

    


