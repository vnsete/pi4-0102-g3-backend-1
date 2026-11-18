package br.servihub;
import java.nio.file.*;
import java.sql.*;
public final class InitializeDatabase {
 public static void main(String[]args)throws Exception {
  try(Connection c=Common.db()){String sql=Files.readString(Path.of(args.length>0?args[0]:"database/001-schema.sql"));for(String command:sql.split(";")){if(!command.isBlank())try(Statement s=c.createStatement()){s.execute(command);}}}
  System.out.println("Estrutura MySQL pronta.");
 }
}
