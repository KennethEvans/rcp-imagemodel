package cbg.article.model;

import java.util.ArrayList;
import java.util.List;

public class MovingBox extends Model
{
    protected List<MovingBox> boxes;
    protected List<BoardGame> games;
    protected List<Book> books;

    private static IModelVisitor adder = new Adder();
    private static IModelVisitor remover = new Remover();

    public MovingBox() {
        boxes = new ArrayList<MovingBox>();
        games = new ArrayList<BoardGame>();
        books = new ArrayList<Book>();
    }

    private static class Adder implements IModelVisitor
    {

        /*
         * @see ModelVisitorI#visitBoardgame(BoardGame)
         */

        /*
         * @see ModelVisitorI#visitBook(MovingBox)
         */

        /*
         * @see ModelVisitorI#visitMovingBox(MovingBox)
         */

        /*
         * @see ModelVisitorI#visitBoardgame(BoardGame, Object)
         */
        public void visitBoardgame(BoardGame boardgame, Object argument) {
            ((MovingBox)argument).addBoardGame(boardgame);
        }

        /*
         * @see ModelVisitorI#visitBook(MovingBox, Object)
         */
        public void visitBook(Book book, Object argument) {
            ((MovingBox)argument).addBook(book);
        }

        /*
         * @see ModelVisitorI#visitMovingBox(MovingBox, Object)
         */
        public void visitMovingBox(MovingBox box, Object argument) {
            ((MovingBox)argument).addBox(box);
        }

    }

    private static class Remover implements IModelVisitor
    {
        public void visitBoardgame(BoardGame boardgame, Object argument) {
            ((MovingBox)argument).removeBoardGame(boardgame);
        }

        /*
         * @see ModelVisitorI#visitBook(MovingBox, Object)
         */
        public void visitBook(Book book, Object argument) {
            ((MovingBox)argument).removeBook(book);
        }

        /*
         * @see ModelVisitorI#visitMovingBox(MovingBox, Object)
         */
        public void visitMovingBox(MovingBox box, Object argument) {
            ((MovingBox)argument).removeBox(box);
            box.addListener(NullDeltaListener.getSoleInstance());
        }

    }

    public MovingBox(String name) {
        this();
        this.name = name;
    }

    public List<MovingBox> getBoxes() {
        return boxes;
    }

    protected void addBox(MovingBox box) {
        boxes.add(box);
        box.parent = this;
        fireAdd(box);
    }

    protected void addBook(Book book) {
        books.add(book);
        book.parent = this;
        fireAdd(book);
    }

    protected void addBoardGame(BoardGame game) {
        games.add(game);
        game.parent = this;
        fireAdd(game);
    }

    public List<Book> getBooks() {
        return books;
    }

    public void remove(Model toRemove) {
        toRemove.accept(remover, this);
    }

    protected void removeBoardGame(BoardGame boardGame) {
        games.remove(boardGame);
        boardGame.addListener(NullDeltaListener.getSoleInstance());
        fireRemove(boardGame);
    }

    protected void removeBook(Book book) {
        books.remove(book);
        book.addListener(NullDeltaListener.getSoleInstance());
        fireRemove(book);
    }

    protected void removeBox(MovingBox box) {
        boxes.remove(box);
        // Why is this necessary ?
        box.addListener(NullDeltaListener.getSoleInstance());
        fireRemove(box);
    }

    public void add(Model toAdd) {
        toAdd.accept(adder, this);
    }

    public List<BoardGame> getGames() {
        return games;
    }

    /**
     * Answer the total number of items the receiver contains.
     */
    public int size() {
        return getBooks().size() + getBoxes().size() + getGames().size();
    }

    /*
     * @see Model#accept(ModelVisitorI, Object)
     */
    public void accept(IModelVisitor visitor, Object passAlongArgument) {
        visitor.visitMovingBox(this, passAlongArgument);
    }

}
