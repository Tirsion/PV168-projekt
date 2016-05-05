package cz.muni.fi.pv168.libraryloans;

import java.util.List;

/**
 *
 * @author L
 */
public interface LoanManager {
    
    public void createLoan(Loan loan);
    
    public void updateLoan(Loan loan);
    
    public void deleteLoan(Loan loan);
    
    public Loan getLoanById(Long id);
    
    public List<Loan> findAllLoans();
    
    public List<Loan> findAllLoansForReader(Reader reader);
    
    public List<Loan> findAllLoansForBook(Book book);
    
}
