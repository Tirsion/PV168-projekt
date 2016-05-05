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
public class ReaderManagerImplTest {
    
    private ReaderManagerImpl manager;
    private DataSource dataSource;
    
    private static DataSource prepareDataSource() throws SQLException {
        EmbeddedDataSource ds = new EmbeddedDataSource();
        ds.setDatabaseName("memory:librarymanager-test");
        ds.setCreateDatabase("create");
        return ds;
    }
        
    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    
    @Before
    public void setUp() throws SQLException {
        dataSource = prepareDataSource();
        DBUtils.executeSqlScript(dataSource, Reader.class.getResource("createTables.sql"));
        manager = new ReaderManagerImpl();
        manager.setDataSource(dataSource);
    }

    @After
    public void tearDown() throws SQLException {
        DBUtils.executeSqlScript(dataSource, Reader.class.getResource("dropTables.sql"));
    }
    
    //Preparing test data
    private ReaderBuilder sampleReader1() {
        return new ReaderBuilder()
                .id(null)
                .name("Pavel")
                .address("Brno 41")
                .eMail("pavel@mail.cz")
                .note(null);                
    }
    
    private ReaderBuilder sampleReader2() {
        return new ReaderBuilder()
                .id(null)
                .name("Petr s Příjmením")
                .address("Brno 42")
                .eMail("petr@mail.cz")
                .note("Sample note");                
    }
    
    @Test
    public void testCreateReader() {
        Reader reader = sampleReader1().build();
        manager.createReader(reader);
        
        Long readerId = reader.getId();
        assertThat("saved reader has null id", reader.getId(), is(not(equalTo(null))));
        
        Reader result = manager.getReaderById(readerId);
        //loaded instance should be equal to the saved one
        assertThat("loaded reader differs from the saved one", result, is(equalTo(reader)));
        //but it should be another instance
        assertThat("loaded reader is the same instance", result, is(not(sameInstance(reader))));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testCreteWithNull() {
        manager.createReader(null);
    }
    
    @Test
    public void createReaderWithExistingId() {
        //Existing ID
        Reader reader = sampleReader1().id(1L).build();
        expectedException.expect(IllegalEntityException.class);
        manager.createReader(reader);
    }
    
    @Test
    public void createReaderWithWrongName() {
        //Wrong name
        Reader reader = sampleReader1().name("41").build();
        expectedException.expect(IllegalArgumentException.class);
        manager.createReader(reader);
    }
    
    @Test
    public void createReaderWithWrongEMail() {
        //Wrong eMail
        Reader reader = sampleReader1().eMail("pavelmail.cz").build();
        expectedException.expect(IllegalArgumentException.class);
        manager.createReader(reader);    
    }
    
    @Test
    public void findAllReaders() {
        assertTrue(manager.findAllReaders().isEmpty());
        
        Reader r1 = sampleReader1().build();
        Reader r2 = sampleReader2().build();
        
        manager.createReader(r1);
        manager.createReader(r2);
        
        List<Reader> expected = Arrays.asList(r1, r2);
        List<Reader> actual = manager.findAllReaders();
        
        Collections.sort(actual, idComparator);
        Collections.sort(expected, idComparator);
        
        assertEquals("saved and retrieved readers differ", expected, actual);
        assertDeepEquals(expected, actual);
    }
    
    @Test
    public void findReadersByName() {
        assertTrue(manager.findReadersByName("Petr").isEmpty());
        
        Reader r1 = sampleReader1().build();
        Reader r2 = sampleReader2().build();
        Reader r3 = sampleReader1().name("Petr").build();
        Reader r4 = sampleReader2().name("Petr").build();
        
        manager.createReader(r1);
        manager.createReader(r2);
        manager.createReader(r3);
        manager.createReader(r4);
        
        List<Reader> expected = Arrays.asList(r3, r4);
        List<Reader> actual = manager.findReadersByName("Petr");
        
        Collections.sort(actual, idComparator);
        Collections.sort(expected, idComparator);
        
        assertEquals("saved and retrieved readers differ", expected, actual);
        assertDeepEquals(expected, actual);
    }
    
    @FunctionalInterface
    private static interface Operation<T> {
        void callOn(T subjectOfOperation);
    }
   
    private void testUpdateReader(Operation<Reader> updateOperation){
        Reader r1 = sampleReader1().build();
        Reader r2 = sampleReader2().build();
        
        manager.createReader(r1);
        manager.createReader(r2);
        
        updateOperation.callOn(r1);
        
        manager.updateReader(r1);
        
        assertThat(manager.getReaderById(r1.getId()))
                .isEqualToComparingFieldByField(r1);
        assertThat(manager.getReaderById(r2.getId()))
                .isEqualToComparingFieldByField(r2);
    }
    
    @Test
    public void updateReaderName() {
        testUpdateReader(reader -> reader.setName("Karel"));
    }
    
    @Test
    public void updateReaderAddress() {
        testUpdateReader(reader -> reader.setAddress("Nová Adresa 10"));
    }
    
    @Test
    public void updateReadereMail() {
        testUpdateReader(reader -> reader.setEMail("Karel@jinymail.cz"));
    }
    
    @Test
    public void updateReaderNote() {
        testUpdateReader(reader -> reader.setNote("Nová poznámka"));
    }
    
    @Test
    public void updateReaderNameWithWrongValue() {
        expectedException.expect(IllegalArgumentException.class);
        testUpdateReader(reader -> reader.setName("Karel4"));
    }
    
    @Test
    public void deleteReader() {

        Reader r1 = sampleReader1().build();
        Reader r2 = sampleReader2().build();
        
        manager.createReader(r1);
        manager.createReader(r2);

        assertNotNull(manager.getReaderById(r1.getId()));
        assertNotNull(manager.getReaderById(r2.getId()));

        manager.deleteReader(r1);

        assertNull(manager.getReaderById(r1.getId()));
        assertNotNull(manager.getReaderById(r2.getId()));

    }
    
    @Test(expected = IllegalArgumentException.class)
    public void deleteNullReader() {
        manager.deleteReader(null);
    }
    
    @Test
    public void deleteReaderWithNullId() {
        Reader reader = sampleReader1().id(null).build();
        expectedException.expect(IllegalEntityException.class);
        manager.deleteReader(reader);
    }
    
    @Test
    public void deleteReaderWithNonExistingId() {
        Reader reader = sampleReader1().id(1L).build();
        expectedException.expect(IllegalEntityException.class);
        manager.deleteReader(reader);
    }
    
    @Test
    public void createReaderWithSqlExceptionThrown() throws SQLException {
        SQLException sqlException = new SQLException();
        DataSource failingDataSource = mock(DataSource.class);
        when(failingDataSource.getConnection()).thenThrow(sqlException);
        manager.setDataSource(failingDataSource);
        
        Reader reader = sampleReader1().build();
        
        assertThatThrownBy(() -> manager.createReader(reader))
                .isInstanceOf(ServiceFailureException.class)
                .hasCause(sqlException);
    }
    
    private void testExpectedServiceFailureException(Operation<ReaderManager> operation) throws SQLException {
        SQLException sqlException = new SQLException();
        DataSource failingDataSource = mock(DataSource.class);
        when(failingDataSource.getConnection()).thenThrow(sqlException);
        manager.setDataSource(failingDataSource);
        assertThatThrownBy(() -> operation.callOn(manager))
                .isInstanceOf(ServiceFailureException.class)
                .hasCause(sqlException);
    }
    
    @Test
    public void updateReaderWithSqlExceptionThrown() throws SQLException {
        Reader reader = sampleReader1().build();
        manager.createReader(reader);
        testExpectedServiceFailureException(readerManager -> readerManager.updateReader(reader));
    }
    
    @Test
    public void getReaderWithSqlExceptionThrown() throws SQLException {
        Reader reader = sampleReader1().build();
        manager.createReader(reader);
        testExpectedServiceFailureException(readerManager -> readerManager.getReaderById(reader.getId()));
    }
    
    @Test
    public void deleteReaderWithSqlExceptionThrown() throws SQLException {
        Reader reader = sampleReader1().build();
        manager.createReader(reader);
        testExpectedServiceFailureException(readerManager -> readerManager.deleteReader(reader));
    }
    
    @Test
    public void findAllReadersWithSqlExceptionThrown() throws SQLException {
        Reader reader = sampleReader1().build();
        manager.createReader(reader);
        testExpectedServiceFailureException(readerManager -> readerManager.findAllReaders());
    }
    
    @Test
    public void findReadersByNameWithSqlExceptionThrown() throws SQLException {
        Reader reader = sampleReader1().build();
        manager.createReader(reader);
        testExpectedServiceFailureException(readerManager -> readerManager.findReadersByName("Pavel"));
    }
    
    private void assertDeepEquals(List<Reader> expectedList, List<Reader> actualList) {
        for (int i = 0; i < expectedList.size(); i++) {
            Reader expected = expectedList.get(i);
            Reader actual = actualList.get(i);
            assertDeepEquals(expected, actual);
        }
    }
    
    private void assertDeepEquals(Reader expected, Reader actual) {
        assertEquals("id value is not equal",expected.getId(), actual.getId());
        assertEquals("name value is not equal",expected.getName(), actual.getName());
        assertEquals("address value is not equal",expected.getAddress(), actual.getAddress());
        assertEquals("eMail value is not equal",expected.getEMail(), actual.getEMail());
        assertEquals("note value is not equal",expected.getNote(), actual.getNote());
    }
    
    private static Comparator<Reader> idComparator = new Comparator<Reader>() {
        @Override
        public int compare(Reader r1, Reader r2) {
            return r1.getId().compareTo(r2.getId());
        }
    };
    
}
