package in.pwskills.harshit.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebInitParam;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns = { "/registrationPage" }, loadOnStartup = 1, initParams = {
		@WebInitParam(name = "driver", value = "com.mysql.cj.jdbc.Driver"),
		@WebInitParam(name = "url", value = "jdbc:mysql://localhost:3306/pwskills_jdbc"),
		@WebInitParam(name = "user", value = "root"), @WebInitParam(name = "password", value = "root12345") })
public class RegistrationPage extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private static final String SQL_INSERT_QUERY = "INSERT INTO registration (name, email, password) VALUES (?, ?, ?)";
	private static final String SQL_CHECK_USER_EXISTS = "SELECT COUNT(*) FROM registration WHERE email = ?";

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
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();

		String name = request.getParameter("name");
		String email = request.getParameter("email");
		String password = request.getParameter("password");

		if (name == null || email == null || password == null) {
			out.println("<h1>All fields are required!</h1>");
			return;
		}

		try (PreparedStatement checkStmt = connection.prepareStatement(SQL_CHECK_USER_EXISTS)) {
			checkStmt.setString(1, email);
			try (ResultSet rs = checkStmt.executeQuery()) {
				if (rs.next() && rs.getInt(1) > 0) {
					out.println("<h1>email already exists!</h1>");
					return;
				}
			}

			try (PreparedStatement insertStmt = connection.prepareStatement(SQL_INSERT_QUERY)) {
				insertStmt.setString(1, name);
				insertStmt.setString(2, email);
				insertStmt.setString(3, password);

				int result = insertStmt.executeUpdate();
				if (result > 0) {
					out.println("<h1>Registration Successful</h1>");
				} else {
					out.println("<h1>Registration Unsuccessful</h1>");
				}
			} catch (SQLException e) {
				e.printStackTrace();
				out.println("<h1>Internal Server Error</h1>");
			}
		} catch (SQLException e) {
			e.printStackTrace();
			out.println("<h1>Internal Server Error</h1>");
		} finally {
			out.close();
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