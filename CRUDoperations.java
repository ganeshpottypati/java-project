import java.sql.*;
import java.util.Scanner;
import java.io.*;

public class CRUDoperations{
    static boolean flag= true;
   static Scanner sc = new Scanner(System.in);
    static String operation;
    static String TableName;
    public static void main(String[] args) {
        
        String url = "jdbc:mysql://localhost:3306/college_details";
        String username = "root";
        String password = "ganesh123";
       
            
            while(flag)
            {
        System.out.println("Enter operation you like to perform  \n 1 CREATE \n 2 INSERT \n 3 DELETE \n 4 Truncate\n 5 DROP\n 6 select ");
        char ch=sc.next().charAt(0);
        
        switch(ch) {
            case '1':
                    operation="create";
                break;
            case '2':
                    operation="insert";
                break;
                case '3':
                    operation="delete";
                break;
                case '4':
                    operation="truncate";
                break;
                case '5':
                    operation="drop";
                break;
                case '6':
                    operation="select";
                break;
            default:
                System.out.println("Enter the correct choice ");
                continue;
        }
        String fileName = operation.trim() + ".txt";
        try (Connection con = DriverManager.getConnection(url, username, password)) 
        {
            //System.out.println("Connected to Oracle DB!");
             operations(con, fileName,operation);
            
        }
             catch (SQLException e) {
            System.out.println("General Error: " + e.getMessage());
        }
                
        System.out.println("DO you want to perform more operations : (y/n)");
        String s=sc.next();
        if(s.equalsIgnoreCase("y"))
        {
            flag=true;
        }
        else{
            flag=false;
        }
    }
    }

   

    
    public static void operations(Connection con,String fileName,String operation)
    {
        try{
        String fullSQL = readSQLFromFile(fileName);

            String[] statements = fullSQL.split(";");
            System.out.println("AVailable Tables :");
            System.out.println(readSQLFromFile("Tables.txt"));
            System.out.println("select table Name : ");
            char c=sc.next().charAt(0);
            switch(c) {
            case '1':
                    TableName="dept";
                break;
            case '2':
                    TableName="emp";
                break;
            case '3':
                    TableName="bonus";
                break;
            case '4':
                    TableName="salgrade";
                break;
            case '5':
                    userquery(con,operation);
                return;

            default :
                System.out.println("select the correct join among the above mentioned tables ");
                break;
            }
            for(String sql:statements)
            {
            if(operation.equalsIgnoreCase("insert"))
                {
                    if(sql.toLowerCase().contains("insert into "+TableName.toLowerCase()))
                    {
                        check(con,TableName,sql);
                    }
                }
                if(operation.equalsIgnoreCase("create"))
                {
                    if(sql.toLowerCase().contains("table "+TableName.toLowerCase()))
                    {
                        check(con,TableName,sql);
                    }
                }
                else
                {
                    if(sql.toLowerCase().contains(TableName.toLowerCase()))
                    {
                    check(con,TableName,sql);
                    }
                }
            }
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }
    public static void userquery(Connection con,String operation)
    {
        System.out.println("Enter the table name that you are wnated to perform :");
        TableName=sc.next();
        sc.nextLine();
        System.out.println("Enter Query to perform "+operation.toUpperCase()+" operation on "+TableName.toUpperCase()+" table");
        String query=sc.nextLine();
        System.out.println("simple");
        check(con,TableName,query);
    }

    
    public static void check(Connection con,String TableName,String sql)
    {
                sql = sql.trim();
                if (!sql.isEmpty()) {
                    try (Statement stmt = con.createStatement()) {
                        if(sql.contains("select"))
                        {
                           ResultSet rs=stmt.executeQuery(sql);
                           System.out.println("Executed: " + sql);
                            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            System.out.println("Fetched Data:");
            System.out.println("-------------");

            while (rs.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    System.out.print(metaData.getColumnLabel(i) + ": " + rs.getString(i) + "\t");
                }
                System.out.println();
            }
        }
        else{
            stmt.executeUpdate(sql);
                           System.out.println("Executed: " + sql);
                    } 
                }catch (SQLException e) {
                        System.out.println("Error executing: " + sql);
                        System.out.println(e.getMessage());
                    }
                }
                else
                {
                    System.out.println("you didn't entered the query, Enter somthing to perform ");
                    sql=sc.next();
                    check(con,TableName,sql);
                }
            }
       
        
    

    private static String readSQLFromFile(String filename) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                // Skip comments starting with --
                if (!line.trim().startsWith("--")) {
                    sb.append(line).append("\n");
                }
            }
        }
        return sb.toString().trim();
    }
}