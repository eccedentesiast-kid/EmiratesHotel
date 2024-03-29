package fr.emirashotel;

import fr.emirashotel.model.*;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Singular;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.NoSuchElementException;

public class DatabaseManager {

    private static Connection connection;

    @Getter
    private static SQLConfig sqlConfig;

    public static void create(SQLConfig sqlConfig) throws SQLException, ClassNotFoundException{
        if(connection != null) return;
        DatabaseManager.sqlConfig = sqlConfig;
        Class.forName("com.mysql.cj.jdbc.Driver");
        System.out.println(sqlConfig.getUsername());
        connection = DriverManager.getConnection(sqlConfig.getUrl(), sqlConfig.getUsername(), sqlConfig.getPassword());
    }

    public static void destroy() throws SQLException {
        if(connection == null) return;
        connection.close();
        connection = null;
    }

    public static ArrayList<Employee> getEmployees() throws SQLException {
        if(connection == null) return null;
        Statement statement = connection.createStatement();
        String requete = "SELECT * FROM employee JOIN person USING(personID) ORDER BY personID ASC";
        ResultSet resultset = statement.executeQuery(requete);
        ArrayList<Employee> employees = new ArrayList<>();

        while (resultset.next()){
            employees.add(
                    Employee.builder()
                            .id(resultset.getLong("personID"))
                            .name(resultset.getString("name"))
                            .mail(resultset.getString("mail"))
                            .dateOfBirth(resultset.getDate("dateOfBirth"))
                            .address(resultset.getString("address"))
                            .contractStart(resultset.getDate("contractStart"))
                            .role(resultset.getString("role"))
                            .build()
            );
        }
        resultset.close();
        statement.close();
        return employees;
    }

    public static ArrayList<Customer> getCustomers() throws SQLException {
        if(connection == null) return null;
        Statement statement = connection.createStatement();
        String requete = "SELECT * FROM customer JOIN person USING(personID) ORDER BY personID ASC";
        ResultSet resultset = statement.executeQuery(requete);
        ArrayList<Customer> customers = new ArrayList<>();

        while (resultset.next()){
            customers.add(
                    Customer.builder()
                            .id(resultset.getLong("personID"))
                            .name(resultset.getString("name"))
                            .mail(resultset.getString("mail"))
                            .dateOfBirth(resultset.getDate("dateOfBirth"))
                            .address(resultset.getString("address"))
                            .joiningDate(resultset.getDate("joiningDate"))
                            .build()
            );
        }
        resultset.close();
        statement.close();
        return customers;
    }

    public static ArrayList<Room> getRooms() throws SQLException{
        if(connection == null) return null;
        Statement statement = connection.createStatement();
        String requete = "SELECT * FROM room";
        ResultSet resultset = statement.executeQuery(requete);
        ArrayList<Room> rooms = new ArrayList<>();

        while (resultset.next()){
            RoomType roomType = RoomType.Double;
            try {
                roomType = RoomType.valueOf(resultset.getString("type"));
            }catch (NoSuchElementException e){
                System.err.println("Erreur : Impossible de trouver le type:"+resultset.getString("type"));
            }
            rooms.add(Room.builder()
                    .id(resultset.getLong("RoomID"))
                    .type(roomType)
                    .number(resultset.getInt("number"))
                    .price(resultset.getFloat("price"))
                    .build()
            );
        }
        resultset.close();
        statement.close();
        return rooms;
    }

    /**
     * Récupérer liste des chambres qui sont disponible entre 2 dates
     */
    public static ArrayList<Room> getRooms(Date start, Date end) throws SQLException {
        if(connection == null) return null;
        Statement statement = connection.createStatement();
        String requete = "SELECT * FROM room " +
                "WHERE RoomID NOT IN ( " +
                "SELECT RoomID FROM room R " +
                "JOIN bookingroom ON (R.RoomID = room) " +
                "WHERE start >= ? and start <= ? " +
                "or end >= ? and end <= ?);";

        PreparedStatement preparedStatement = connection.prepareStatement(requete);

        preparedStatement.setDate(1,new java.sql.Date(start.getTime()));
        preparedStatement.setDate(2,new java.sql.Date(start.getTime()));
        preparedStatement.setDate(3,new java.sql.Date(end.getTime()));
        preparedStatement.setDate(4,new java.sql.Date(end.getTime()));

        ResultSet resultset = preparedStatement.executeQuery();
        ArrayList<Room> rooms = new ArrayList<>();

        while (resultset.next()){
            RoomType roomType = RoomType.Double;
            try {
                roomType = RoomType.valueOf(resultset.getString("type"));
            }catch (NoSuchElementException e){
                System.err.println("Erreur : Impossible de trouver le type:"+resultset.getString("type"));
            }
            rooms.add(Room.builder()
                    .id(resultset.getLong("RoomID"))
                    .type(roomType)
                    .number(resultset.getInt("number"))
                    .price(resultset.getFloat("price"))
                    .build()
            );
        }
        resultset.close();
        statement.close();
        return rooms;
    }

    public static ArrayList<FoodDish> getDishes() throws SQLException{
        if(connection == null) return null;
        Statement statement = connection.createStatement();
        String requete = "SELECT * FROM fooddish";
        ResultSet resultset = statement.executeQuery(requete);
        ArrayList<FoodDish> dishes = new ArrayList<>();

        while (resultset.next()){
            dishes.add(FoodDish.builder()
                    .id(resultset.getLong("DishID"))
                    .dishName(resultset.getString("dishName"))
                    .dishType(resultset.getString("dishType"))
                    .description(resultset.getString("description"))
                    .price(resultset.getFloat("price"))
                    .build()
            );
        }
        resultset.close();
        statement.close();
        return dishes;
    }

    public static ArrayList<BookingRoom> bookingRooms() throws SQLException {
        if(connection == null) return null;
        Statement statement = connection.createStatement();
        String requete = "SELECT * FROM bookingroom JOIN customer C On (C.PersonID = customer) JOIN person P USING (PersonID) JOIN room R ON (R.RoomID = room)";
        ResultSet resultset = statement.executeQuery(requete);
        ArrayList<BookingRoom> bookingRooms = new ArrayList<>();

        while (resultset.next()){
            Customer customer = Customer.builder()
                    .id(resultset.getLong("personID"))
                    .name(resultset.getString("name"))
                    .mail(resultset.getString("mail"))
                    .dateOfBirth(resultset.getDate("dateOfBirth"))
                    .address(resultset.getString("address"))
                    .joiningDate(resultset.getDate("joiningDate"))
                    .build();

            RoomType roomType = RoomType.Double;
            try {
                roomType = RoomType.valueOf(resultset.getString("type"));
            }catch (NoSuchElementException e){
                System.err.println("Erreur : Impossible de trouver le type:"+resultset.getString("type"));
            }
            Room room = Room.builder()
                    .id(resultset.getLong("RoomID"))
                    .type(roomType)
                    .number(resultset.getInt("number"))
                    .price(resultset.getFloat("price"))
                    .build();

            bookingRooms.add(BookingRoom.builder()
                    .id(resultset.getLong("bookingID"))
                    .start(resultset.getDate("start"))
                    .end(resultset.getDate("end"))
                    .customer(customer)
                    .room(room)
                    .build()
            );
        }
        resultset.close();
        statement.close();
        return bookingRooms;
    }

    public static ArrayList<BookingRoom> bookingRooms(java.sql.Date dateBooking) throws SQLException {
        if(connection == null) return null;
        String requete = "SELECT * FROM bookingroom JOIN customer C On (C.PersonID = customer) " +
                                                    "JOIN person P USING (PersonID) " +
                                                    "JOIN room R ON (R.RoomID = room) " +
                         "WHERE ? BETWEEN bookingroom.start AND bookingroom.end";

        PreparedStatement preparedStatement = connection.prepareStatement(requete);
        preparedStatement.setDate(1, dateBooking);

        ResultSet resultset = preparedStatement.executeQuery();
        ArrayList<BookingRoom> bookingRooms = new ArrayList<>();

        while (resultset.next()){
            Customer customer = Customer.builder()
                    .id(resultset.getLong("personID"))
                    .name(resultset.getString("name"))
                    .mail(resultset.getString("mail"))
                    .dateOfBirth(resultset.getDate("dateOfBirth"))
                    .address(resultset.getString("address"))
                    .joiningDate(resultset.getDate("joiningDate"))
                    .build();

            RoomType roomType = RoomType.Double;
            try {
                roomType = RoomType.valueOf(resultset.getString("type"));
            }catch (NoSuchElementException e){
                System.err.println("Erreur : Impossible de trouver le type:"+resultset.getString("type"));
            }
            Room room = Room.builder()
                    .id(resultset.getLong("RoomID"))
                    .type(roomType)
                    .number(resultset.getInt("number"))
                    .price(resultset.getFloat("price"))
                    .build();

            bookingRooms.add(BookingRoom.builder()
                    .id(resultset.getLong("bookingID"))
                    .start(resultset.getDate("start"))
                    .end(resultset.getDate("end"))
                    .customer(customer)
                    .room(room)
                    .build()
            );
        }
        resultset.close();
        preparedStatement.close();
        return bookingRooms;
    }

    public static ArrayList<BookingRestaurant> bookingDishes() throws SQLException {
        if(connection == null) return null;
        Statement statement = connection.createStatement();
        String requete = "SELECT * FROM bookingrestaurant JOIN customer C On (C.PersonID = customer) JOIN person P USING (PersonID) JOIN fooddish F ON (F.DishID = dish)";
        ResultSet resultset = statement.executeQuery(requete);
        ArrayList<BookingRestaurant> bookingRestaurants = new ArrayList<>();

        while (resultset.next()){
            Customer customer = Customer.builder()
                    .id(resultset.getLong("personID"))
                    .name(resultset.getString("name"))
                    .mail(resultset.getString("mail"))
                    .dateOfBirth(resultset.getDate("dateOfBirth"))
                    .address(resultset.getString("address"))
                    .joiningDate(resultset.getDate("joiningDate"))
                    .build();

            FoodDish dish = FoodDish.builder()
                    .id(resultset.getLong("DishID"))
                    .dishName(resultset.getString("dishName"))
                    .dishType(resultset.getString("dishType"))
                    .description(resultset.getString("description"))
                    .price(resultset.getFloat("price"))
                    .build();


            bookingRestaurants.add(BookingRestaurant.builder()
                    .id(resultset.getLong("bookingID"))
                    .customer(customer)
                    .dish(dish)
                    .quantity(resultset.getInt("quantity"))
                    .build()
            );
        }
        resultset.close();
        statement.close();
        return bookingRestaurants;
    }

    //Add
    public static boolean addEmployee(Employee employee) throws SQLException {
        if (connection != null) {
            String addPersonquery = "INSERT INTO person (PersonID, NAME, mail, dateOfBirth, address) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement prepare = connection.prepareStatement(addPersonquery);
            prepare.setString(1, String.valueOf(employee.getId()));
            prepare.setString(2, employee.getName());
            prepare.setString(3, employee.getMail());
            prepare.setString(4, String.valueOf(employee.getDateOfBirth()));
            prepare.setString(5, employee.getAddress());
            prepare.executeUpdate();

            String addEmployeeQuery = "INSERT INTO employee (PersonID, contractStart, role) VALUES (?, ?, ?)";
            PreparedStatement prepareCustomer = connection.prepareStatement(addEmployeeQuery);
            prepareCustomer.setString(1, String.valueOf(employee.getId()));
            prepareCustomer.setString(2, String.valueOf(employee.getContractStart()));
            prepareCustomer.setString(3, String.valueOf(employee.getRole()));
            prepareCustomer.executeUpdate();
            return true;
        }
        return false;
    }

    public static boolean addCustomer(Customer customer) throws  SQLException {
        if (connection != null) {
            String addPersonquery = "INSERT INTO person (PersonID, NAME, mail, dateOfBirth, address) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement prepare = connection.prepareStatement(addPersonquery);
            prepare.setString(1, String.valueOf(customer.getId()));
            prepare.setString(2, customer.getName());
            prepare.setString(3, customer.getMail());
            prepare.setString(4, String.valueOf(customer.getDateOfBirth()));
            prepare.setString(5, customer.getAddress());
            prepare.executeUpdate();

            String addCustomerQuery = "INSERT INTO customer (PersonID, joiningDate) VALUES (?, ?)";
            PreparedStatement prepareCustomer = connection.prepareStatement(addCustomerQuery);
            prepareCustomer.setString(1, String.valueOf(customer.getId()));
            prepareCustomer.setString(2, String.valueOf(customer.getJoiningDate()));
            prepareCustomer.executeUpdate();
            return true;
        }
        return false;
    }

    //Update
    public static boolean updateCustomer(Customer customer) throws SQLException {
        if (connection != null) {
            String updatePersonQuery = "UPDATE person SET " +
                    "NAME = '" + customer.getName()
                    + "', mail = '" + customer.getMail()
                    + "', dateOfBirth = '" + String.valueOf(customer.getDateOfBirth())
                    + "', address = '" + customer.getAddress()
                    + "' WHERE PersonID = '" + String.valueOf(customer.getId()) + "'";
            Statement preparePerson = connection.createStatement();
            preparePerson.executeUpdate(updatePersonQuery);

            String updateCustomerQuery = "UPDATE customer SET " +
                    "joiningDate = '" + String.valueOf(customer.getJoiningDate())
                    + "' WHERE PersonID = '" + String.valueOf(customer.getId()) + "'";
            Statement prepareCustomer = connection.createStatement();
            prepareCustomer.executeUpdate(updateCustomerQuery);
            return true;
        }
        return false;
    }

    public static boolean updateEmployee(Employee employee) throws SQLException {
        if (connection != null) {
            String updatePersonQuery = "UPDATE person SET " +
                    "NAME = '" + employee.getName()
                    + "', mail = '" + employee.getMail()
                    + "', dateOfBirth = '" + String.valueOf(employee.getDateOfBirth())
                    + "', address = '" + employee.getAddress()
                    + "' WHERE PersonID = '" + String.valueOf(employee.getId()) + "'";
            Statement preparePerson = connection.createStatement();
            preparePerson.executeUpdate(updatePersonQuery);

            String updateEmployeeQuery = "UPDATE employee SET " +
                    "contractStart = '" + String.valueOf(employee.getContractStart())
                    + "', role = '" + employee.getRole()
                    + "' WHERE PersonID = '" + String.valueOf(employee.getId()) + "'";
            Statement prepareCustomer = connection.createStatement();
            prepareCustomer.executeUpdate(updateEmployeeQuery);
            return true;
        }
        return false;
    }

    //Delete
    public static boolean deleteCustomer(Customer customer) throws SQLException {
        if (connection != null) {
            String deletePersonQuery = "DELETE FROM person WHERE PersonID = '" + customer.getId().toString() + "'";
            Statement preparePerson = connection.createStatement();
            preparePerson.executeUpdate(deletePersonQuery);

            String deleteCustomerQuery = "DELETE FROM customer WHERE PersonID = '" + customer.getId().toString() + "'";
            Statement prepareCustomer = connection.createStatement();
            prepareCustomer.executeUpdate(deleteCustomerQuery);
            return true;
        }
        return false;
    }

    public static boolean deleteEmployee(Employee employee) throws SQLException {
        if (connection != null) {
            String deletePersonQuery = "DELETE FROM person WHERE PersonID = '" + employee.getId().toString() + "'";
            Statement preparePerson = connection.createStatement();
            preparePerson.executeUpdate(deletePersonQuery);

            String deleteEmployeeQuery = "DELETE FROM employee WHERE PersonID = '" + employee.getId().toString() + "'";
            Statement prepareCustomer = connection.createStatement();
            prepareCustomer.executeUpdate(deleteEmployeeQuery);
            return true;
        }
        return false;
    }

    public static long getNumberOfRows(String table) throws SQLException {
        long rowCount = 0;
        if (connection != null) {
            String query = "SELECT COUNT(*) AS row_count FROM " + table;
            Statement prepare = connection.createStatement();
            ResultSet resultSet = prepare.executeQuery(query);

            if (resultSet.next()) {
                rowCount = resultSet.getInt("row_count");
            }
        }
        return rowCount;
    }

    public static boolean checkID(Long PersonID) throws SQLException {
        String checkData = "SELECT PersonID FROM person WHERE PersonID = '" + PersonID.toString() + "'";
        Statement prepare = connection.createStatement();
        ResultSet resultSet = prepare.executeQuery(checkData);

        if (resultSet.next()) {
            return true;
        }
        return false;
    }

    public static boolean addBookingRoom(BookingRoom booking) throws SQLException{
        if (connection != null){
            String addPersonquery = "INSERT INTO BookingRoom (bookingID, customer, start, end, room) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement prepare = connection.prepareStatement(addPersonquery);
            prepare.setString(1, String.valueOf(booking.getId()));
            prepare.setString(2, String.valueOf(booking.getCustomerID()));
            prepare.setString(3, String.valueOf(booking.getStart()));
            prepare.setString(4, String.valueOf(booking.getEnd()));
            prepare.setString(5, String.valueOf(booking.getRoomID()));
            prepare.executeUpdate();
            return true;
        }else{
            return false;
        }
    }

    public static boolean addBookingRestaurant(BookingRestaurant booking) throws SQLException{
        if (connection != null){
            String addPersonquery = "INSERT INTO BookingRestaurant (bookingID, customer, dish, quantity) VALUES (?, ?, ?, ?)";
            PreparedStatement prepare = connection.prepareStatement(addPersonquery);
            prepare.setString(1, String.valueOf(booking.getId()));
            prepare.setString(2, String.valueOf(booking.getCustomerID()));
            prepare.setString(3, String.valueOf(booking.getFoodID()));
            prepare.setString(4, String.valueOf(booking.getQuantity()));
            prepare.executeUpdate();
            return true;
        }else{
            return false;
        }
    }
}
