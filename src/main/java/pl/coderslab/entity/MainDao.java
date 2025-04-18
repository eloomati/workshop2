package pl.coderslab.entity;


public class MainDao {
    public static void main(String[] args) {
        User user = new User();
        UserDao userDao = new UserDao();

        user.setUserName("testUser");
        user.setEmail("test@mail.com");
        user.setPassword("123");

        userDao.create(user);
    }
}
