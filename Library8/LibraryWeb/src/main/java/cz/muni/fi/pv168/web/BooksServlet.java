package cz.muni.fi.pv168.web;

import cz.muni.fi.pv168.libraryloans.*;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Level;

/**
 * Servlet for managing books.
 *
 * @author L
 */
@WebServlet(BooksServlet.URL_MAPPING + "/*")
public class BooksServlet extends HttpServlet {

    private static final String LIST_JSP = "/list.jsp";
    public static final String URL_MAPPING = "/books";

    private final static Logger log = Logger.getLogger(BooksServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        log.log(Level.INFO, "GET ...");
        showBooksList(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //support non-ASCII characters in form
        request.setCharacterEncoding("utf-8");
        //action specified by pathInfo
        String action = request.getPathInfo();
        log.log(Level.INFO, "POST ... {}", action);//concatenation ok?
        switch (action) {
            case "/add":
                //getting POST parameters from form
                String title = request.getParameter("title");
                String author = request.getParameter("author");
                String publishedStr = request.getParameter("published");
                String note = request.getParameter("note");
                int published;
                if (publishedStr.matches("[0-9]+")) {
                    published = Integer.parseInt(publishedStr);
                }
                //form data validity check
                else {
                    request.setAttribute("chyba", "publishing year must be integer !");
                    log.log(Level.INFO, "form data invalid");
                    showBooksList(request, response);
                    return;
                }
                if (title == null || title.length() == 0 || author == null || author.length() == 0) { //doplnit kontrolu roku
                    request.setAttribute("chyba", "all values except note must be filled !");
                    log.log(Level.INFO, "form data invalid");
                    showBooksList(request, response);
                    return;
                }
                //form data processing - storing to database
                try {
                    Book book = new Book();
                    book.setTitle(title);
                    book.setAuthor(author);
                    book.setPublished(published);
                    book.setNote(note);
                    getBookManager().createBook(book);
                    //redirect-after-POST protects from multiple submission
                    log.log(Level.INFO, "redirecting after POST");
                    response.sendRedirect(request.getContextPath()+URL_MAPPING);
                    return;
                } catch (ServiceFailureException e) {
                    String msg ="Cannot add book";
                    log.log(Level.SEVERE, msg, e);
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
                    return;
                }
            case "/delete":
                try {
                    Long id = Long.valueOf(request.getParameter("id"));
                    Book book = getBookManager().getBookById(id);
                    getBookManager().deleteBook(book);
                    log.log(Level.INFO, "redirecting after POST");
                    response.sendRedirect(request.getContextPath()+URL_MAPPING);
                    return;
                } catch (ServiceFailureException e) {
                    String msg ="Cannot delete book";
                    log.log(Level.SEVERE, msg, e);
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
                    return;
                }
            case "/update":
                //TODO
                return;
            default:
                String msg ="Unknown action " + action;
                log.log(Level.SEVERE, msg);
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Unknown action " + action);
        }
    }

    /**
     * Gets BookManager from ServletContext, where it was stored by {@link StartListener}.
     *
     * @return BookManager instance
     */
    private BookManager getBookManager() {
        return (BookManager) getServletContext().getAttribute("bookManager");
    }

    /**
     * Stores the list of books to request attribute "books" and forwards to the JSP to display it.
     */
    private void showBooksList(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            log.log(Level.INFO, "showing table of books");
            request.setAttribute("books", getBookManager().findAllBooks());
            request.getRequestDispatcher(LIST_JSP).forward(request, response);
        } catch (ServiceFailureException e) {
            String msg ="Cannot show book";
            log.log(Level.SEVERE, msg, e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}
