package cz.muni.fi.pv168.libraryloans;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 *
 * @author L
 */
public class Loan {
    
    private Long id;
    private Reader reader;
    private Book book;
    private LocalDate startDate;
    private LocalDate expectedEndDate;
    private LocalDateTime realEndTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Reader getReader() {
        return reader;
    }

    public void setReader(Reader reader) {
        this.reader = reader;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getExpectedEndDate() {
        return expectedEndDate;
    }

    public void setExpectedEndDate(LocalDate expectedEnd) {
        this.expectedEndDate = expectedEnd;
    }

    public LocalDateTime getRealEndTime() {
        return realEndTime;
    }

    public void setRealEndTime(LocalDateTime realEndTime) {
        this.realEndTime = realEndTime;
    }

    //Without times
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 61 * hash + Objects.hashCode(this.id);
        hash = 61 * hash + Objects.hashCode(this.reader);
        hash = 61 * hash + Objects.hashCode(this.book);
        return hash;
    }
    
    //Without times
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Loan other = (Loan) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        if (!Objects.equals(this.reader, other.reader)) {
            return false;
        }
        if (!Objects.equals(this.book, other.book)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Loan{" + "id=" + id + ", reader=" + reader + ", book=" + book + ", startTime=" + startDate + ", expectedEndTime=" + expectedEndDate + ", realEndTime=" + realEndTime + '}';
    }   

}
