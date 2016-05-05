package cz.muni.fi.pv168.libraryloans;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.sql.DataSource;
import org.apache.derby.jdbc.EmbeddedDataSource;
import org.junit.*;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import org.junit.rules.ExpectedException;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 *
 * @author L
 */
public class BookManagerImplTest {

    private BookManagerImpl manager;
    private DataSource ds;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private static DataSource prepareDataSource() throws SQLException {
        EmbeddedDataSource ds = new EmbeddedDataSource();
        ds.setDatabaseName("memory:librarymanager-test");
        ds.setCreateDatabase("create");
        return ds;
    }

    @Before
    public void setUp() throws SQLException {
        ds = prepareDataSource();
        DBUtils.executeSqlScript(ds,Book.class.getResource("createTables.sql"));
        manager = new BookManagerImpl();
        manager.setDataSource(ds);
    }

    @After
    public void tearDown() throws SQLException {
        DBUtils.executeSqlScript(ds,Book.class.getResource("dropTables.sql"));
    }

    private BookBuilder sampleBookBuilder1() {
        return new BookBuilder()
                .id(null)
                .title("Jako cool v plotě")
                .author("Plíhal Karel")
                .published(2006)
                .note("poezie");
    }

    private BookBuilder sampleBookBuilder2() {
        return new BookBuilder()
                .id(null)
                .title("Thinking in java")
                .author("Eckel Bruce")
                .published(2002)
                .note("");
    }

    @Test
    public void createBook() {
        Book book = sampleBookBuilder1().build();
        manager.createBook(book);

        Long bookId = book.getId();
        assertThat("book has null id", book.getId(), is(not(equalTo(null))));
        
        Book result = manager.getBookById(bookId);
        assertThat("loaded instance should be equal to the saved one", result, is(book));
        assertThat("loaded instance should be another instance", result, not(sameInstance(book)));
        assertDeepEquals(book, result);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void createNullBook() {
        manager.createBook(null);
    }
    
    @Test
    public void createBookWithExistingId() {
        Book book = sampleBookBuilder1().id(1L).build();
        expectedException.expect(IllegalEntityException.class);
        manager.createBook(book);
    }
    
    @Test
    public void createBookWithEmptyTitle() {
        Book book = sampleBookBuilder1().title("").build();
        expectedException.expect(IllegalArgumentException.class);
        manager.createBook(book);
    }
    
    @Test
    public void createBookWithEmptyAuthor() {
        Book book = sampleBookBuilder1().author("").build();
        expectedException.expect(IllegalArgumentException.class);
        manager.createBook(book);
    }
    
    @Test
    public void createBookWithWrongAuthorName() {
        Book book = sampleBookBuilder1().author("15648").build();
        expectedException.expect(IllegalArgumentException.class);
        manager.createBook(book);
    }
    
//    @Test
//    public void testFindBookById() {
//        Book book = newBook("Effective Java", "Bloch Joshua", 2008, "education-IT");
//        manager.createBook(book);
//        Long bookId = book.getId();
//        assertThat("book has null id", book.getId(), notNullValue());
//        
//        Book result = manager.getBookById(bookId);
//        
//        assertThat("two book with the same id should be equal", result, is(book));
//        assertDeepEquals(book, result);
//    }
//    
//        @Test(expected = NullPointerException.class)
//    public void testFindBookByIdWithNull() throws Exception {
//        manager.getBookById(null);
//    }
    
    @Test
    public void findAllBooks() {

        assertTrue(manager.findAllBooks().isEmpty());

        Book b1 = sampleBookBuilder1().build();
        Book b2 = sampleBookBuilder2().build();

        manager.createBook(b1);
        manager.createBook(b2);
        
        List<Book> expected = Arrays.asList(b1, b2);
        List<Book> actual = manager.findAllBooks();
        
        Collections.sort(actual, idComparator);
        Collections.sort(expected, idComparator);

        assertEquals(expected, actual);
        assertDeepEquals(expected, actual);
    }
    
    @FunctionalInterface
    private static interface Operation<T> {
        void callOn(T subjectOfOperation);
    }

    private void testUpdateBook(Operation<Book> updateOperation) {
        Book sourceBook = sampleBookBuilder1().build();
        Book anotherBook = sampleBookBuilder2().build();
        
        manager.createBook(sourceBook);
        manager.createBook(anotherBook);

        updateOperation.callOn(sourceBook);

        manager.updateBook(sourceBook);
        
        assertThat(manager.getBookById(sourceBook.getId()))
                .isEqualToComparingFieldByField(sourceBook);
        assertThat(manager.getBookById(anotherBook.getId()))
                .isEqualToComparingFieldByField(anotherBook);
    }

    @Test
    public void updateBookTitle() {
        testUpdateBook((book) -> book.setTitle("Java"));
    }

    @Test
    public void updateBookAuthor() {
        testUpdateBook((book) -> book.setAuthor("Bloch"));
    }
    
    @Test
    public void updateBookPublished() {
        testUpdateBook((book) -> book.setPublished(2000));
    }

    @Test
    public void updateBookNote() {
        testUpdateBook((book) -> book.setNote("educational"));
    }

    @Test
    public void updateBookNoteToNull() {
        testUpdateBook((book) -> book.setNote(null));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void updateNullBook() {
        manager.updateBook(null);
    }

    @Test
    public void updateBookWithNullId() {
        Book book = sampleBookBuilder1().id(null).build();
        expectedException.expect(IllegalEntityException.class);
        manager.updateBook(book);
    }

    @Test
    public void updateBookWithNonExistingId() {
        Book book = sampleBookBuilder1().id(1L).build();
        expectedException.expect(IllegalEntityException.class);
        manager.updateBook(book);
    }

    @Test
    public void updateBookWithEmptyTitle() {
        Book book = sampleBookBuilder1().build();
        manager.createBook(book);
        book.setTitle("");
        expectedException.expect(IllegalArgumentException.class);
        manager.updateBook(book);
    }
    
    @Test
    public void updateBookWithEmptyAuthor() {
        Book book = sampleBookBuilder1().build();
        manager.createBook(book);
        book.setAuthor("");
        expectedException.expect(IllegalArgumentException.class);
        manager.updateBook(book);
    }    
    
    @Test
    public void updateBookWithWrongAuthorName() {
        Book book = sampleBookBuilder1().build();
        manager.createBook(book);
        book.setAuthor("1569");
        expectedException.expect(IllegalArgumentException.class);
        manager.updateBook(book);
    }
    
    @Test
    public void deleteBook() {
        Book b1 = sampleBookBuilder1().build();
        Book b2 = sampleBookBuilder2().build();
        
        manager.createBook(b1);
        manager.createBook(b2);

        assertNotNull(manager.getBookById(b1.getId()));
        assertNotNull(manager.getBookById(b2.getId()));

        manager.deleteBook(b1);

        assertNull(manager.getBookById(b1.getId()));
        assertNotNull(manager.getBookById(b2.getId()));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void deleteBookWithNull() throws Exception {
        manager.deleteBook(null);
    }
    
    @Test
    public void deleteBookWithNullId() {
        Book book = sampleBookBuilder1().id(null).build();
        expectedException.expect(IllegalEntityException.class);
        manager.deleteBook(book);
    }

    @Test
    public void deleteBookWithNonExistingId() {
        Book book = sampleBookBuilder1().id(1L).build();
        expectedException.expect(IllegalEntityException.class);
        manager.deleteBook(book);
    }
    
    @Test
    public void createBookWithSqlExceptionThrown() throws SQLException {
        SQLException sqlException = new SQLException();
        DataSource failingDataSource = mock(DataSource.class);
        when(failingDataSource.getConnection()).thenThrow(sqlException);
        manager.setDataSource(failingDataSource);

        Book book = sampleBookBuilder1().build();

        assertThatThrownBy(() -> manager.createBook(book))
                .isInstanceOf(ServiceFailureException.class)
                .hasCause(sqlException);
    }

    private void testExpectedServiceFailureException(Operation<BookManager> operation) throws SQLException {
        SQLException sqlException = new SQLException();
        DataSource failingDataSource = mock(DataSource.class);
        when(failingDataSource.getConnection()).thenThrow(sqlException);
        manager.setDataSource(failingDataSource);
        assertThatThrownBy(() -> operation.callOn(manager))
                .isInstanceOf(ServiceFailureException.class)
                .hasCause(sqlException);
    }

    @Test
    public void updateBookWithSqlExceptionThrown() throws SQLException {
        Book book = sampleBookBuilder1().build();
        manager.createBook(book);
        testExpectedServiceFailureException((bookManager) -> bookManager.updateBook(book));
    }

    @Test
    public void getBookWithSqlExceptionThrown() throws SQLException {
        Book book = sampleBookBuilder1().build();
        manager.createBook(book);
        testExpectedServiceFailureException((bookManager) -> bookManager.getBookById(book.getId()));
    }

    @Test
    public void deleteBookWithSqlExceptionThrown() throws SQLException {
        Book book = sampleBookBuilder1().build();
        manager.createBook(book);
        testExpectedServiceFailureException((bookManager) -> bookManager.deleteBook(book));
    }

    @Test
    public void findAllBooksWithSqlExceptionThrown() throws SQLException {
        testExpectedServiceFailureException((bookManager) -> bookManager.findAllBooks());
    }

    private void assertDeepEquals(Book expected, Book actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getTitle(), actual.getTitle());
        assertEquals(expected.getAuthor(), actual.getAuthor());
        assertEquals(expected.getPublished(), actual.getPublished());
        assertEquals(expected.getNote(), actual.getNote());
        }
    
    private void assertDeepEquals(List<Book> expectedList, List<Book> actualList) {
        for (int i = 0; i < expectedList.size(); i++) {
            Book expected = expectedList.get(i);
            Book actual = actualList.get(i);
            assertDeepEquals(expected, actual);
        }
    }
    
    private static Comparator<Book> idComparator = new Comparator<Book>() {
        @Override
        public int compare(Book b1, Book b2) {
            return b1.getId().compareTo(b2.getId());
        }
    };    
}


