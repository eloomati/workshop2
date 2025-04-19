package pl.coderslab.entity;

import org.mindrot.jbcrypt.BCrypt;
import pl.coderslab.DbUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;

public class UserDao {

    private static final String CREATE_USER_QUERY =
            "INSERT INTO users(username, email, password) VALUES (?, ?, ?)";
    private static final String READ_USER_QUERY =
            "SELECT * FROM users WHERE id = ?";
    private static final String UPDATE_USER_QUERY =
            "UPDATE users SET  email = ?, username = ?, password = ? WHERE id = ?";
    private static final String DELETE_USER_QUERY =
            "DELETE FROM users WHERE id = ?";
    private static final String FIND_ALL_USERS_QUERY =
            "SELECT * FROM users";

    public static void lastKeyValue(Connection conn, String sql) throws SQLException {
        try (PreparedStatement preStmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ResultSet rs = preStmt.getGeneratedKeys();
            if (rs.next()) {
                long id = rs.getLong(1);
                System.out.println("Inserted id: " + id);
            }
        }
    }

    public String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    public User create(User user) {
        try (Connection conn = DbUtil.getConnection()) {
            PreparedStatement statement =
                    conn.prepareStatement(CREATE_USER_QUERY, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, user.getUserName());
            statement.setString(2, user.getEmail());
            statement.setString(3, hashPassword(user.getPassword()));
            statement.executeUpdate();
            //Pobieramy wstawiony do bazy identyfikator, a następnie ustawiamy id obiektu user.
            ResultSet resultSet = statement.getGeneratedKeys();
            if (resultSet.next()) {
                user.setId(resultSet.getInt(1));
            }
            return user;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public User read(int id){
        try(Connection conn = DbUtil.getConnection()){

            PreparedStatement statement =
                    conn.prepareStatement(READ_USER_QUERY);
            statement.setInt(1, id);
            try(ResultSet resultSet = statement.executeQuery()){
                // Pobieranie i ustawianie wartości obiektu user
                if (resultSet.next()){ // pobieramy tylko jeden wiersz
                    User user = new User();
                    user.setId(resultSet.getInt("id"));
                    user.setUserName(resultSet.getString("username"));
                    user.setEmail(resultSet.getString("email"));
                    user.setPassword(resultSet.getString("password"));
                    return user;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void update(User user){
        try (Connection conn = DbUtil.getConnection()){
            PreparedStatement statement =
                    conn.prepareStatement(UPDATE_USER_QUERY);
            statement.setString(1, user.getEmail());
            statement.setString(2, user.getUserName());
            statement.setString(3, hashPassword(user.getPassword()));
            statement.setInt(4, user.getId());
            statement.executeUpdate();
        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    public void delete(int id){
        try (Connection conn = DbUtil.getConnection()){
            PreparedStatement statement =
                    conn.prepareStatement(DELETE_USER_QUERY);
            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    public User[] findAll(){
        try (Connection conn = DbUtil.getConnection()){
            PreparedStatement statement =
                    conn.prepareStatement(FIND_ALL_USERS_QUERY, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            try(ResultSet resultSet = statement.executeQuery()){
                // Liczenie liczby wierszy
                resultSet.last(); // Przejście do ostatniego wiersza
                int rowCount = resultSet.getRow(); // Pobranie numeru wiersza (liczba wierszy)
                resultSet.beforeFirst(); // Powrót do początku wyników

                // Tworzenie tablicy o rozmiarze rowCount
                User[] users = new User[rowCount];
                int index = 0;

                while (resultSet.next()){
                    User user = new User();
                    user.setId(resultSet.getInt("id"));
                    user.setUserName(resultSet.getString("username"));
                    user.setEmail(resultSet.getString("email"));
                    user.setPassword(resultSet.getString("password"));
                    users[index] = user;
                    index++;
                }

                return users;
            }
        } catch (SQLException e){
            e.printStackTrace();
        }
        return null;
    }

    //metoda pomocnicza, która poprzez kopiowanie tablic pozwoli nam poradzić sobie ze zwiększającym rozmiarem danych

    private User[] addToArray(User u, User[] users) {
        User[] tmpUsers = Arrays.copyOf(users, users.length + 1); // Tworzymy kopię tablicy powiększoną o 1.
        tmpUsers[users.length] = u; // Dodajemy obiekt na ostatniej pozycji.
        return tmpUsers; // Zwracamy nową tablicę.
    }

    public User[] findAllWithOtherMethod() {
        try (Connection conn = DbUtil.getConnection()) {
            PreparedStatement statement = conn.prepareStatement(FIND_ALL_USERS_QUERY);
            try (ResultSet resultSet = statement.executeQuery()) {
                User[] users = new User[0]; // Inicjalizacja pustej tablicy

                while (resultSet.next()) {
                    User user = new User();
                    user.setId(resultSet.getInt("id"));
                    user.setUserName(resultSet.getString("username"));
                    user.setEmail(resultSet.getString("email"));
                    user.setPassword(resultSet.getString("password"));
                    users = addToArray(user, users); // Dodanie użytkownika do tablicy
                }

                return users;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new User[0]; // Zwracamy pustą tablicę w przypadku błędu
    }

}
