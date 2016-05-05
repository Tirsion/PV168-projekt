package cz.muni.fi.pv168.libraryGUI;

import cz.muni.fi.pv168.libraryloans.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author xskvari1
 */
public class LoansTableModel extends AbstractTableModel {

    private List<Loan> loans = new ArrayList<>();

    public LoansTableModel(LoanManagerImpl manager) {
        this.loans = manager.findAllLoans();
    }
    
    @Override
    public int getRowCount() {
        return loans.size();
    }

    @Override
    public int getColumnCount() {
        return 6;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Loan loan = loans.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return loan.getId();
            case 1:
                return loan.getReader().getId();
            case 2:
                return loan.getBook().getId();
            case 3:
                return loan.getStartDate();
            case 4:
                return loan.getExpectedEndDate();
            case 5:
                return loan.getRealEndTime();
            default:
                throw new IllegalArgumentException("columnIndex");
        }
    }

    @Override
    public String getColumnName(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return "Loan Id";
            case 1:
                return "Reader Id";
            case 2:
                return "Book Id";
            case 3:
                return "Start date";
            case 4:
                return "Expected end";
            case 5:
                return "Real end";
            default:
                throw new IllegalArgumentException("columnIndex");
        }
    }

}
