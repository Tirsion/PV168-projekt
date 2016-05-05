package cz.muni.fi.pv168.libraryloans;

import java.sql.SQLException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import static java.time.Month.*;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.sql.DataSource;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.sameInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.apache.derby.jdbc.EmbeddedDataSource;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 *
 * @author L et M
 */
public class LoanManagerImplTest {
    
    private LoanManagerImpl loanManager;
    private ReaderManagerImpl readerManager;
    private BookManagerImpl bookManager;
    private DataSource ds;
    
    private final BookBuilder preparedBookBuilder = new BookBuilder()
                .id(null)
                .title("Jako cool v plotě")
                .author("Plíhal Karel")
                .published(2006)
                .note("poezie");
    
    private final ReaderBuilder preparedReaderBuilder = new ReaderBuilder()
                .id(null)
                .name("Petr s Příjmením")
                .address("Brno 42")
                .eMail("petr@mail.cz")
                .note("Sample note");
    
    private final static ZonedDateTime NOW
        = LocalDateTime.of(2016, MARCH, 27, 14, 00).atZone(ZoneId.of("UTC"));
    
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private static DataSource prepareDataSource() throws SQLException {
        EmbeddedDataSource ds = new EmbeddedDataSource();
        ds.setDatabaseName("memory:librarymanager-test");
        ds.setCreateDatabase("create");
        return ds;
    }

    private static Clock prepareClockMock(ZonedDateTime now) {
        return Clock.fixed(now.toInstant(), now.getZone());
    }
    
    @Before
    public void setUp() throws SQLException {
        ds = prepareDataSource();
        DBUtils.executeSqlScript(ds,Loan.class.getResource("createTables.sql"));
        loanManager = new LoanManagerImpl(prepareClockMock(NOW));
        loanManager.setDataSource(ds);
        readerManager = new ReaderManagerImpl();
        readerManager.setDataSource(ds);
        bookManager = new BookManagerImpl();
        bookManager.setDataSource(ds);
        loanManager.setReaderManager(readerManager);
        loanManager.setBookManager(bookManager);
    }

    @After
    public void tearDown() throws SQLException {
        DBUtils.executeSqlScript(ds,Loan.class.getResource("dropTables.sql"));
    }

    private LoanBuilder sampleLoanBuilder1() {
        Reader reader1 = new ReaderBuilder()
                .id(null)
                .name("Pavel")
                .address("Brno 41")
                .eMail("pavel@mail.cz")
                .note(null)
                .build();
        readerManager.createReader(reader1);
        Book book1 = new BookBuilder()
                .id(null)
                .title("Thinking in java")
                .author("Eckel Bruce")
                .published(2002)
                .note("")
                .build();
        bookManager.createBook(book1);
        return new LoanBuilder()
                .id(null)
                .reader(reader1)
                .book(book1)
                .startDate(LocalDate.of(2015, SEPTEMBER, 19))
                .expectedEndDate(LocalDate.of(2015, OCTOBER, 19))
                .realEndTime(LocalDateTime.of(2015, 10, 18, 11, 23));
    }

    private LoanBuilder sampleLoanBuilder2() {
        Reader reader2 = new ReaderBuilder()
                .id(null)
                .name("Petr s Příjmením")
                .address("Brno 42")
                .eMail("petr@mail.cz")
                .note("Sample note")
                .build();
        readerManager.createReader(reader2);
        Book book2 = new BookBuilder()
                .id(null)
                .title("Jako cool v plotě")
                .author("Plíhal Karel")
                .published(2006)
                .note("poezie")
                .build();
        bookManager.createBook(book2);
        return new LoanBuilder()
                .id(null)
                .reader(reader2)
                .book(book2)
                .startDate(LocalDate.of(2016, JANUARY, 5))
                .expectedEndDate(LocalDate.of(2016, FEBRUARY, 5))
                .realEndTime(LocalDateTime.of(2016, 1, 27, 13, 25));
    }
    
    @Test
    public void createLoan() {
        Loan loan = sampleLoanBuilder1().build();
        loanManager.createLoan(loan);

        Long loanId = loan.getId();
        assertThat("loan has null id", loan.getId(), is(not(equalTo(null))));
        
        Loan result = loanManager.getLoanById(loanId);
        assertThat("loaded instance should be equal to the saved one", result, is(equalTo(loan)));
        assertThat("loaded instance should be another instance", result, is(not(sameInstance(loan))));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void createNullLoan() throws Exception {
        loanManager.createLoan(null);
    }

    @Test
    public void createLoanWithExistingId() {
        Loan loan = sampleLoanBuilder1().id(1L).build();
        expectedException.expect(IllegalEntityException.class);
        loanManager.createLoan(loan);
    }
    
    @Test
    public void createLoanExpectedEndDateBeforeStartDate() {
        Loan loan = sampleLoanBuilder1()
                .startDate(2016,FEBRUARY,12)
                .expectedEndDate(2016,FEBRUARY,10)
                .build();
        expectedException.expect(ValidationException.class);
        loanManager.createLoan(loan);
    }
    
    @Test
    public void createLoanWithStartDateInFuture() {
        LocalDate tomorrow = NOW.toLocalDate().plusDays(1);
        Loan loan = sampleLoanBuilder1()
                .startDate(tomorrow)
                .build();
        assertThatThrownBy(() -> loanManager.createLoan(loan))
                .isInstanceOf(ValidationException.class);
    }
    
    @Test
    public void findAllLoans() {
        assertTrue(loanManager.findAllLoans().isEmpty());
        
        Loan l1 = sampleLoanBuilder1().build();
        Loan l2 = sampleLoanBuilder2().build();
        
        loanManager.createLoan(l1);
        loanManager.createLoan(l2);
        
        List<Loan> expected = Arrays.asList(l1, l2);
        List<Loan> actual = loanManager.findAllLoans();
        
        Collections.sort(actual, idComparator);
        Collections.sort(expected, idComparator);
        
        assertEquals("saved and retrieved loans differ", expected, actual);
        assertDeepEquals(expected, actual);
    }
    
    @FunctionalInterface
    private static interface Operation<T> {
        void callOn(T subjectOfOperation);
    }
   
    private void testUpdateLoan(Operation<Loan> updateOperation){
        Loan l1 = sampleLoanBuilder1().build();
        Loan l2 = sampleLoanBuilder2().build();
        
        loanManager.createLoan(l1);
        loanManager.createLoan(l2);
        
        updateOperation.callOn(l1);
        
        loanManager.updateLoan(l1);
        
        assertThat(loanManager.getLoanById(l1.getId()))
                .isEqualToComparingFieldByField(l1);
        assertThat(loanManager.getLoanById(l2.getId()))
                .isEqualToComparingFieldByField(l2);
    }
    
    @Test
    public void updateLoanReader() {
        Reader reader = preparedReaderBuilder.build();
        readerManager.createReader(reader);
        testUpdateLoan(loan -> loan.setReader(reader));
    }
    
    @Test
    public void updateLoanBook() {
        Book book = preparedBookBuilder.build();
        bookManager.createBook(book);
        testUpdateLoan(loan -> loan.setBook(book));
    }
    
    @Test
    public void updateLoanStartDate() {
        testUpdateLoan(loan -> loan.setStartDate(LocalDate.of(2002, MARCH, 3)));
    }
    
    @Test
    public void updateLoanExpectedEndDate() {
        testUpdateLoan(loan -> loan.setExpectedEndDate(LocalDate.of(2016, APRIL, 3)));
    }
    
    @Test
    public void updateLoanRealEndTime() {
        testUpdateLoan(loan -> loan.setRealEndTime(LocalDateTime.of(2013, 2, 2, 12, 5)));
    }  
    
    @Test
    public void updateLoanWithNullId() {
        Loan loan = sampleLoanBuilder1().id(null).build();
        expectedException.expect(IllegalEntityException.class);
        loanManager.updateLoan(loan);
    }

    @Test
    public void updateLoanWithNonExistingId() {
        Loan loan = sampleLoanBuilder1().id(1L).build();
        expectedException.expect(IllegalEntityException.class);
        loanManager.updateLoan(loan);
    }    
    
    @Test
    public void updateLoanWithExpectedEndDateBeforeStartDate() {
        Loan loan = sampleLoanBuilder1().startDate(2016,FEBRUARY,12).expectedEndDate(2016,MARCH,12).build();
        loanManager.createLoan(loan);
        loan.setStartDate(LocalDate.of(2016,MARCH,14));
        
        expectedException.expect(ValidationException.class);
        loanManager.updateLoan(loan);
    }
    
    @Test
    public void deleteLoan() {

        Loan l1 = sampleLoanBuilder1().build();
        Loan l2 = sampleLoanBuilder2().build();
        
        loanManager.createLoan(l1);
        loanManager.createLoan(l2);

        assertNotNull(loanManager.getLoanById(l1.getId()));
        assertNotNull(loanManager.getLoanById(l2.getId()));

        loanManager.deleteLoan(l1);

        assertNull(loanManager.getLoanById(l1.getId()));
        assertNotNull(loanManager.getLoanById(l2.getId()));

    }
    
    @Test(expected = IllegalArgumentException.class)
    public void deleteNullLoan() {
        loanManager.deleteLoan(null);
    }
    
    @Test
    public void deleteLoanWithNullId() {
        Loan loan = sampleLoanBuilder1().id(null).build();
        expectedException.expect(IllegalEntityException.class);
        loanManager.deleteLoan(loan);
    }
    
    @Test
    public void deleteLoanWithNonExistingId() {
        Loan loan = sampleLoanBuilder1().id(1L).build();
        expectedException.expect(IllegalEntityException.class);
        loanManager.deleteLoan(loan);
    }
    
    @Test
    public void findAllLoansForReader() {
        Reader r1 = preparedReaderBuilder.build();
        Reader r2 = preparedReaderBuilder.build();
        Book b1 = preparedBookBuilder.build();
        bookManager.createBook(b1);
        readerManager.createReader(r1);
        readerManager.createReader(r2);
        
        assertTrue(loanManager.findAllLoansForReader(r1).isEmpty());
        
        Loan l1 = new LoanBuilder().reader(r1).book(b1).build();
        Loan l2 = new LoanBuilder().reader(r1).book(b1).build();
        Loan l3 = new LoanBuilder().reader(r2).book(b1).build();
        Loan l4 = new LoanBuilder().reader(r2).book(b1).build();
        
        loanManager.createLoan(l1);
        loanManager.createLoan(l2);
        loanManager.createLoan(l3);
        loanManager.createLoan(l4);
        
        List<Loan> expected = Arrays.asList(l1, l2);
        List<Loan> actual = loanManager.findAllLoansForReader(r1);
        
        Collections.sort(actual, idComparator);
        Collections.sort(expected, idComparator);
        
        assertEquals("saved and retrieved loans differ", expected, actual);
        assertDeepEquals(expected, actual);
    }
    
    @Test
    public void findAllLoansForBook() {
        Book b1 = preparedBookBuilder.build();
        Book b2 = preparedBookBuilder.build();
        Reader r1 = preparedReaderBuilder.build();
        bookManager.createBook(b2);
        bookManager.createBook(b1);
        readerManager.createReader(r1);
        
        assertTrue(loanManager.findAllLoansForBook(b1).isEmpty());
        
        Loan l1 = new LoanBuilder().book(b1).reader(r1).build();
        Loan l2 = new LoanBuilder().book(b1).reader(r1).build();
        Loan l3 = new LoanBuilder().book(b2).reader(r1).build();
        Loan l4 = new LoanBuilder().book(b2).reader(r1).build();
        
        loanManager.createLoan(l1);
        loanManager.createLoan(l2);
        loanManager.createLoan(l3);
        loanManager.createLoan(l4);
        
        List<Loan> expected = Arrays.asList(l1, l2);
        List<Loan> actual = loanManager.findAllLoansForBook(b1);
        
        Collections.sort(actual, idComparator);
        Collections.sort(expected, idComparator);
        
        assertEquals("saved and retrieved loans differ", expected, actual);
        assertDeepEquals(expected, actual);
    }    
    
    @Test
    public void createLoanWithSqlExceptionThrown() throws SQLException {
        SQLException sqlException = new SQLException();
        DataSource failingDataSource = mock(DataSource.class);
        when(failingDataSource.getConnection()).thenThrow(sqlException);
        loanManager.setDataSource(failingDataSource);
        
        Loan loan = sampleLoanBuilder1().build();
        
        assertThatThrownBy(() -> loanManager.createLoan(loan))
                .isInstanceOf(ServiceFailureException.class)
                .hasCause(sqlException);
    }
    
    private void testExpectedServiceFailureException(LoanManagerImplTest.Operation<LoanManager> operation) throws SQLException {
        SQLException sqlException = new SQLException();
        DataSource failingDataSource = mock(DataSource.class);
        when(failingDataSource.getConnection()).thenThrow(sqlException);
        loanManager.setDataSource(failingDataSource);
        assertThatThrownBy(() -> operation.callOn(loanManager))
                .isInstanceOf(ServiceFailureException.class)
                .hasCause(sqlException);
    }
    
    @Test
    public void updateLoanWithSqlExceptionThrown() throws SQLException {
        Loan loan = sampleLoanBuilder1().build();
        loanManager.createLoan(loan);
        testExpectedServiceFailureException(loanManager -> loanManager.updateLoan(loan));
    }
    
    @Test
    public void getLoanWithSqlExceptionThrown() throws SQLException {
        Loan loan = sampleLoanBuilder1().build();
        loanManager.createLoan(loan);
        testExpectedServiceFailureException(loanManager -> loanManager.getLoanById(loan.getId()));
    }
    
    @Test
    public void deleteLoanWithSqlExceptionThrown() throws SQLException {
        Loan loan = sampleLoanBuilder1().build();
        loanManager.createLoan(loan);
        testExpectedServiceFailureException(loanManager -> loanManager.deleteLoan(loan));
    }
    
    @Test
    public void findAllLoansWithSqlExceptionThrown() throws SQLException {
        Loan loan = sampleLoanBuilder1().build();
        loanManager.createLoan(loan);
        testExpectedServiceFailureException(loanManager -> loanManager.findAllLoans());
    }
    
    @Test
    public void findAllLoansForReaderSqlExceptionThrown() throws SQLException {
        Loan loan = sampleLoanBuilder1().build();
        loanManager.createLoan(loan);
        testExpectedServiceFailureException(loanManager -> loanManager.findAllLoansForReader(null));
    }
    
    @Test
    public void findAllLoansForBookSqlExceptionThrown() throws SQLException {
        Loan loan = sampleLoanBuilder1().build();
        loanManager.createLoan(loan);
        testExpectedServiceFailureException(loanManager -> loanManager.findAllLoansForBook(null));
    }
    
    private void assertDeepEquals(List<Loan> expectedList, List<Loan> actualList) {
        for (int i = 0; i < expectedList.size(); i++) {
            Loan expected = expectedList.get(i);
            Loan actual = actualList.get(i);
            assertDeepEquals(expected, actual);
        }
    }
    
    private void assertDeepEquals(Loan expected, Loan actual) {
        assertEquals("id value is not equal",expected.getId(), actual.getId());
        assertEquals("reader value is not equal",expected.getReader(), actual.getReader());
        assertEquals("book value is not equal",expected.getBook(), actual.getBook());
        assertEquals("start date value is not equal",expected.getStartDate(), actual.getStartDate());
        assertEquals("expected end date value is not equal",expected.getExpectedEndDate(), actual.getExpectedEndDate());
        assertEquals("real end time value is not equal",expected.getRealEndTime(), actual.getRealEndTime());

    }
    
    private static Comparator<Loan> idComparator = new Comparator<Loan>() {
        @Override
        public int compare(Loan r1, Loan r2) {
            return r1.getId().compareTo(r2.getId());
        }
    };
}
