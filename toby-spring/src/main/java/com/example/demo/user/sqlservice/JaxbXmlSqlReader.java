//package com.example.demo.user.sqlservice;
//
//import com.example.demo.user.repository.UserDao;
//import java.io.InputStream;
//import javax.xml.bind.JAXBContext;
//import javax.xml.bind.JAXBException;
//import javax.xml.bind.Unmarshaller;
//import javax.xml.bind.annotation.XmlType.DEFAULT;
//
//public class JaxbXmlSqlReader implements SqlReader {
//
//    private static final String DEFAULT_SQLMAP_FIEL = "sqlmap.xml";
//
//    private String sqlmapFile = DEFAULT_SQLMAP_FIEL;
//
//    public void setSqlmapFile(String sqlmapFile) { this.sqlmapFile = sqlmapFile; }
//
//    public void read(SqlRegistry sqlRegistry) {
//        String contextPath = com.epril.sqlmap.Sqlmap.class.getPackage().getName();
//        try {
//            JAXBContext context = JAXBContext.newInstance(contextPath);
//            Unmarshaller unmarshaller = context.createUnmarshaller();
//            InputStream is = UserDao.class.getResourceAsStream(sqlmapFile);
//            com.epril.sqlmap.Sqlmap sqlmap = (com.epril.sqlmap.Sqlmap)unmarshaller.unmarshal(is);
//            for(SqlType sql : sqlmap.getSql()) {
//                sqlRegistry.registerSql(sql.getKey(), sql.getValue());
//            }
//        } catch (JAXBException e) { throw new RuntimeException(e); }
//    }
//
//}
