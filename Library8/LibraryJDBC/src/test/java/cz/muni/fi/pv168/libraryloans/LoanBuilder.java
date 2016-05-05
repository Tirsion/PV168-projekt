/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.muni.fi.pv168.libraryloans;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;

/**
 *
 * @author marcela
 */
public class LoanBuilder {
    
    private Long id;
    private Reader reader;
    private Book book;
    private LocalDate startDate;
    private LocalDate expectedEndDate;
    private LocalDateTime realEndTime;    
    
    public LoanBuilder id(Long id) {
        this.id = id;
        return this;
    }

    public LoanBuilder reader(Reader reader) {
        this.reader = reader;
        return this;
    }

    public LoanBuilder book(Book book) {
        this.book = book;
        return this;
    }

    public LoanBuilder startDate(LocalDate startDate) {
        this.startDate = startDate;
        return this;
    }
    
    public LoanBuilder startDate(int year, Month month, int day) {
        this.startDate = LocalDate.of(year, month, day);
        return this;
    }

    public LoanBuilder expectedEndDate(LocalDate expectedEndDate) {
        this.expectedEndDate = expectedEndDate;
        return this;
    }
    
    public LoanBuilder expectedEndDate(int year, Month month, int day) {
        this.expectedEndDate = LocalDate.of(year, month, day);
        return this;
    }
    
    public LoanBuilder realEndTime(LocalDateTime realEndTime) {
        this.realEndTime = realEndTime;
        return this;
    }    

    public Loan build() {
        Loan loan = new Loan();
        loan.setId(id);
        loan.setReader(reader);
        loan.setBook(book);
        loan.setStartDate(startDate);
        loan.setExpectedEndDate(expectedEndDate);
        loan.setRealEndTime(realEndTime);
        return loan;
    }
}
