package in.pwskills.harshit.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.annotation.WebInitParam;
import jakarta.servlet.annotation.WebServlet;

@WebServlet(urlPatterns = { "/login" }, loadOnStartup = 1, initParams = {
		@WebInitParam(name = "driver", value = "com.mysql.cj.jdbc.Driver"),
		@WebInitParam(name = "url", value = "jdbc:mysql://localhost:3306/pwskills_jdbc"),
		@WebInitParam(name = "user", value = "root"), @WebInitParam(name = "password", value = "root12345") })
public class login extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private static final String SQL_SELECT_QUERY = "SELECT * FROM registration WHERE email = ? AND password = ?";

	private Connection connection = null;

	@Override
	public void init() {
		try {
			Class.forName(getInitParameter("driver"));
			connection = DriverManager.getConnection(getInitParameter("url"), getInitParameter("user"),
					getInitParameter("password"));
			System.out.println("Database connection established.");
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		PrintWriter out = response.getWriter();

		String email = request.getParameter("email");
		String password = request.getParameter("password");

		if (email == null || password == null || email.isEmpty() || password.isEmpty()) {
			out.println("<h1>All fields are required!</h1>");
			return;
		}

		try (PreparedStatement selectStmt = connection.prepareStatement(SQL_SELECT_QUERY)) {
			selectStmt.setString(1, email);
			selectStmt.setString(2, password);

			try (ResultSet rs = selectStmt.executeQuery()) {
				if (rs.next()) {
					out.println("<h1>Login Successful</h1>");
				} else {
					out.println("<h1>Login Unsuccessful</h1>");
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
			out.println("<h1>Internal Server Error</h1>");
		} finally {
			out.close();
		}
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		PrintWriter out = response.getWriter();

		String fname = request.getParameter("fname");
		String femail = request.getParameter("femail");
		String fpassword = request.getParameter("fpassword");

		if (fname == null || femail == null || fpassword == null || fname.isEmpty() || femail.isEmpty()
				|| fpassword.isEmpty()) {
			out.println("<h1>All fields are required!</h1>");
			return;
		}

		String sql_check_user_exists = "SELECT COUNT(*) FROM registration WHERE email = ?";

		try (PreparedStatement checkStmt = connection.prepareStatement(sql_check_user_exists)) {
			checkStmt.setString(1, femail);
			try (ResultSet rs = checkStmt.executeQuery()) {
				if (rs.next() && rs.getInt(1) > 0) {
					String sql_user_update = "update registration set password=? where email=?";
					try (PreparedStatement checkstmtup = connection.prepareStatement(sql_user_update)) {
						checkstmtup.setString(1, fpassword);
						checkstmtup.setString(2, femail);
						Integer rsUp = checkstmtup.executeUpdate();
						if (rsUp == 0) {
							out.println("<h1>Not Updated</h1>");
						} else {
							out.println("");
							out.println("<h1 style=\"text-align: center;\">updated Successfully</h1>");
							out.println("<form action=\"./login.html\">");
							out.println("<table align=\"center\" class=\"form2\">");
							out.println("<tr>");
							out.println("<th>");
							out.println("<button type=\"submit\">LOGIN</button>");
							out.println("</th>");
							out.println("</tr>");
							out.println("</table>");
							out.println("</form>");
						}

					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
			out.println("<h1>Internal Server Error</h1>");
		}
	}

	@Override
	public void destroy() {
		try {
			if (connection != null && !connection.isClosed()) {
				connection.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		super.destroy();
	}
}
