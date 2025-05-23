import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;



public class SGBD {
    
    private DBConfig dbConfig;
    private DiskManager diskManager;
    private BufferManager bufferManager;
    private DBManager dbManager;

    public SGBD(DBConfig dbconfig)  {
        this.dbConfig = dbconfig;
        this.diskManager = new DiskManager(dbconfig);
        this.bufferManager = new BufferManager(this.dbConfig, this.diskManager);
        this.dbManager = new DBManager(dbconfig, diskManager, bufferManager);
        this.diskManager.loadState();
        this.dbManager.LoadState();
    }

    // Getters & Setters
    public DBConfig getDbConfig() {
        return dbConfig;
    }
    public void setDbConfig(DBConfig dbConfig) {
        this.dbConfig = dbConfig;
    }

    public DiskManager getDiskManager() {
        return diskManager;
    }
    public void setDiskManager(DiskManager diskManager) {
        this.diskManager = diskManager;
    }

    public BufferManager getBufferManager() {
        return bufferManager;
    }
    public void setBufferManager(BufferManager bufferManager) {
        this.bufferManager = bufferManager;
    }

    public DBManager getDbManager() {
        return dbManager;
    }
    public void setDbManager(DBManager dbManager) {
        this.dbManager = dbManager;
    }

    public void Run()     {
        System.out.println("\n****************************************    NOTRE SGBD - OUR DBMS   ****************************************");
        System.out.println("****************************************     BIENVENU  - WELECOME   ****************************************");

        String texteCommande;
        boolean quit = false;
        Scanner sc = new Scanner(System.in);
       
        while (!quit) {
            System.out.println("");
            System.out.print("Entrez votre requête ici >>  ");
            texteCommande = sc.nextLine();
            texteCommande = texteCommande.replaceAll("\\s+", " ").trim();
            String start;
            System.out.println("");
            pr:
            if (true){
                if (texteCommande.toUpperCase().startsWith("CREATE DATABASE "))   {
                    try {
                        ProcessCreateDataBaseCommand(texteCommande);
                    }   catch (IOException e)   {
                        System.out.println(e.getMessage());
                    } 
                    break pr;
                }
                if (texteCommande.toUpperCase().startsWith("CREATE TABLE ")) {
                    try {
                        ProcessCreateTableCommand(texteCommande);
                    }   catch (IOException e)   {
                        System.out.println(e.getMessage());
                    } 
                    break pr;
                }
                if (texteCommande.toUpperCase().startsWith("SET DATABASE "))     {
                    try {
                        ProcessSetDataBaseCommand(texteCommande);
                    }   catch (IOException e)   {
                        System.out.println(e.getMessage());
                    }
                    break pr;
                }
                if (texteCommande.toUpperCase().equals("LIST TABLES"))  {
                    ProcessListTablesCommand(texteCommande);
                    break pr;
                }
                if (texteCommande.toUpperCase().equals("LIST DATABASES"))   {
                    ProcessListDataBasesCommand(texteCommande);
                    break pr;
                }
                if (texteCommande.toUpperCase().startsWith("DROP TABLE "))   {
                    try {
                        ProcessDropTableCommand(texteCommande);
                    }   catch (IOException e)   {
                        System.out.println(e.getMessage());
                    }
                    break pr;
                }
                if (texteCommande.toUpperCase().equals("DROP TABLES"))   {
                    ProcessDropTablesCommand(texteCommande);
                    break pr;
                }
                if (texteCommande.toUpperCase().equals("DROP DATABASES"))   {
                    ProcessDropDataBasesCommand(texteCommande);
                    break pr;
                }
                if (texteCommande.toUpperCase().startsWith("DROP DATABASE "))    {
                    try {
                        ProcessDropDataBaseCommand(texteCommande);
                    }   catch (IOException e)   {
                        System.out.println(e.getMessage());
                    }
                    break pr;
                }
                if (texteCommande.toUpperCase().startsWith("INSERT INTO ")) {
                    try    {
                        ProcessInsertIntoCommand(texteCommande);
                    }   catch (IOException e)   {
                        System.out.println(e.getMessage());
                    }
                    break pr;
                }
                if (texteCommande.toUpperCase().startsWith("BULKINSERT INTO ")) {
                    try{
                        ProcessBulkinsertIntoCommand(texteCommande);
                    }   catch (IOException e)   {
                        System.out.println(e.getMessage());
                    }
                    break pr;
                }
                if (texteCommande.toUpperCase().startsWith("SELECT "))  {
                    try {
                        ProcessSelectCommand(texteCommande);
                    }   catch(IOException e)    {
                        System.out.println(e.getMessage());
                    }
                    break pr;
                }
                if (texteCommande.toUpperCase().equals("QUIT")) {
                    ProcessQuitCommand(texteCommande);
                    quit = true;
                    break;
                }
                System.err.println("Erreur : Erreur de syntaxe dans " + texteCommande + " !!");
            }
        }
        System.out.println("****************************************    AU REVOIR - GOODBYE   ****************************************");    
        sc.close();
    }

    public void ProcessCreateDataBaseCommand(String texteCommande) throws IOException {
        String [] splitCommand = texteCommande.split(" ");
        if (splitCommand.length==3) {
            this.dbManager.CreateDatabase(splitCommand[2].toUpperCase());
        }   else {
            throw new IOException("Erreur de syntaxe dans la commande CREATE DATABASE. Syntaxe à suivre : \"CREATE DATABASE X\" où X est remplacé par le nom de la nouvelle base de données.");
        }
    }

    public void ProcessCreateTableCommand(String texteCommande)  throws IOException {
        if (this.dbManager.getActiveDB()==null) {
            throw new IOException("Erreur : Aucune base de données active.");
        }
        String [] splitCommand = texteCommande.split(" ");
        if (splitCommand.length==4) {
            String tableName = splitCommand[2].toUpperCase();
            String[] colonnesEntry = splitCommand[3].substring(1, splitCommand[3].length()-1).split(",");
            ColInfo[] colonnes = new ColInfo[colonnesEntry.length];
            for (int i=0; i<colonnesEntry.length; i++)  {
                String[] nameAndType = colonnesEntry[i].split(":");
                if (nameAndType.length==2){
                    String nom = nameAndType[0].toLowerCase();
                    int taille = 1;
                    String type = null;
                    if (nameAndType[1].toUpperCase().startsWith("CHAR(") || nameAndType[1].toUpperCase().startsWith("VARCHAR("))    {
                        type = nameAndType[1].substring(0, nameAndType[1].indexOf("(")).toUpperCase();
                        if (nameAndType[1].indexOf("(")==-1 || nameAndType[1].indexOf(")")==-1)  {
                            throw new IOException("Erreur de syntaxe dans " + nameAndType[1] + " ! précisez la taille du " + type + " !!");
                        }
                        try{
                            taille = Integer.parseInt( nameAndType[1].substring( nameAndType[1].indexOf("(")+1 , nameAndType[1].indexOf(")") ) );
                        } catch(NumberFormatException e)    {
                            System.err.println("Erreur de syntaxe dans " + nameAndType[1] + " ! la partie entre parenthèses doit correspondre à la taille du "+ type+ " et doit être un entier.");
                        }
                    }   else if (nameAndType[1].toUpperCase().equals("INT"))    {
                        type = "INT";
                    }   else if (nameAndType[1].toUpperCase().equals("REAL"))   {
                        type = "REAL";
                    }   else{
                        throw new IOException("Erreur de syntaxe : le type \"" + nameAndType[1] + "\" n'est pas géré par notre SGBD");
                    }
                    colonnes[i] = new ColInfo(nom, type, taille);
                }  else {
                    throw new IOException("Erreur de syntaxe dans \"" + colonnesEntry[i] + "\"");
                }
            }
            Relation table = new Relation(tableName, colonnesEntry.length, colonnes, null, this.diskManager, this.bufferManager);
            table.initializeHeaderPage();
            this.dbManager.AddTableToCurrentDatabase(table);
        }   else {
            throw new IOException("Erreur de syntaxe dans la commande CREATE TABLE. Syntaxe à suivre : \"CREATE TABLE X (C1:T1,C2:T2(4),C3:T3)\" \noù X est remplacé par le nom de la nouvelle table, C1 le nom du premier attribut, T1 son type (INT,REAL,CHAR(n),VARCHAR(n))...");
        }
    }

    public void ProcessSetDataBaseCommand(String texteCommande)  throws IOException    {
        String [] splitCommand = texteCommande.split(" ");
        if (splitCommand.length==3) {
            this.dbManager.SetCurrentDatabase(splitCommand[2].toUpperCase());
        }   else {
            throw new IOException("Erreur de syntaxe dans la commande SET DATABASE. Syntaxe à suivre : \"SET DATABASE X\" où X est remplacé par le nom d'une base de données existante.");
        }
    }

    public void ProcessListTablesCommand(String texteCommande)      {
        this.dbManager.ListTablesInCurrentDatabase();
    }

    public void ProcessListDataBasesCommand(String texteCommande)   {
        this.dbManager.ListDatabasesNames();
    }

    public void ProcessDropTableCommand (String texteCommande) throws IOException {
        String [] splitCommand = texteCommande.split(" ");
        if (splitCommand.length==3) {
            this.dbManager.RemoveTableFromCurrentDatabase(splitCommand[2].toUpperCase());
        }   else {
            throw new IOException("Erreur de syntaxe dans la commande DROP TABLE. Syntaxe à suivre : \"DROP TABLE X\" où X est remplacé par le nom de la table à supprimer.");
        }
    }

    public void ProcessDropTablesCommand (String texteCommande)     {
        this.dbManager.RemoveTablesFromCurrentDatabase();
    }

    public void ProcessDropDataBasesCommand (String texteCommande)  {
        this.dbManager.RemoveDatabases();
    }

    public void ProcessDropDataBaseCommand (String texteCommande) throws IOException   {
        String [] splitCommand = texteCommande.split(" ");
        if (splitCommand.length==3) {
            this.dbManager.RemoveDatabase(splitCommand[2].toUpperCase());
        }   else {
            throw new IOException("Erreur de syntaxe dans la commande DROP DATABASE. Syntaxe à suivre : \"DROP DATABASE X\" où X est remplacé par le nom de la base de données à supprimer.");
        }
    }

    public void ProcessInsertIntoCommand (String texteCommande) throws IOException {
        if (this.dbManager.getActiveDB() == null)   {
            throw new IOException("Erreur : Aucune base de données active.");
        }
        String [] splitCommand = texteCommande.split(" ");
        if (splitCommand.length == 5 && splitCommand[3].toUpperCase().equals("VALUES"))   {
            String tableName = splitCommand[2].toUpperCase();
            // Vérifions si la table existe bien dans la bdd active
            if (! this.dbManager.getActiveDB().containsTable(tableName))    {
                throw new IOException("Erreur : la table (" + tableName + ") n'existe pas dans la base de données actuellement active {" + this.dbManager.getActiveDB().getNom() + "}");
            }
            Relation table = this.dbManager.getActiveDB().getTable(tableName);
            String [] values = splitCommand[4].substring(1, splitCommand[4].length()-1).split(",");
            if (table.getNbColonnes() == values.length)    {
                String [] recordValues = new String[values.length];
                for (int i=0; i<values.length;  i+=1)   {
                    String type = table.getColonnes()[i].getType();
                    // On vérifie si les entrées correspondent aux types prédéfinies dans la relation 
                    switch (type)   {
                        case "INT" : 
                            try {
                                int myInt = Integer.parseInt(values[i]);
                            }   catch (NumberFormatException e) {
                                throw new IOException("Erreur : l'attribut n°" + (i+1) + " devrait être un INT.");
                            }
                            recordValues[i] = values[i];
                            break;
                        case "REAL" :
                            try {
                                double myFloat = Double.parseDouble(values[i]);
                            }   catch(NumberFormatException e)  {
                                throw new IOException("Erreur : l'attribut n°" + (i+1) + " devrait être un REAL.");
                            }
                            recordValues[i] = values[i];
                            break;
                        case "CHAR" :
                            if (values[i].startsWith("\"") && values[i].endsWith("\"")) {
                                int taille = table.getColonnes()[i].getTaille();
                                if (values[i].length()==taille+2) {
                                    recordValues[i] = values[i].substring(1, values[i].length()-1);
                                }   else{
                                    throw new IOException("Erreur : l'attribut "+ values[i] + " doit être de taille " + taille + " !");
                                }
                            }   else{
                                throw new IOException("Erreur de syntaxe : un attribut de type CHAR doit être mis entre guillemets");
                            }
                            break;
                        case "VARCHAR" :
                            if (values[i].startsWith("\"") && values[i].endsWith("\"")) {
                                int taille = table.getColonnes()[i].getTaille();
                                if (values[i].length()<=taille+2) {
                                    recordValues[i] = values[i].substring(1, values[i].length()-1);
                                }   else{
                                    throw new IOException("Erreur : l'attribut "+ values[i] + " ne doit pas dépasser la taille " + taille + " !");
                                }
                            }   else{
                                throw new IOException("Erreur de syntaxe : un attribut de type VARCHAR doit être mis entre guillemets");
                            }
                            break;
                        default: 
                            break;
                    }
                }
                Record recordToInsert = new Record(recordValues);
                RecordId insertion = table.InsertRecord(recordToInsert);
                if (insertion!=null)    {
                    System.out.println("Insertion d'un record effectuée avec succées dans la table (" + tableName + ")");
                }   else    {
                    throw new IOException("Insertion échouée ! Erreur produite lors de l'insertion du record.");
                }
            }   else {
                throw new IOException("Erreur : Le nombre d'attributs en entrée ne correspond pas au nombre de colonnes dans la table (" + tableName + ")");
            }
        }   else    {
            throw new IOException("Erreur de syntaxe dans la commande INSERT INTO. Syntaxe à suivre : \"INSERT INTO X VALUES (V1,V2,V3)\" \noù X est remplacé par le nom de la table; et V1,V2,V3 correspondent aux valeurs d'un n-uplet de la table, qui seront listés dans le même ordre choisi lors de la création de la table.");
        }
    }

    public void ProcessBulkinsertIntoCommand (String texteCommande) throws IOException {
        if (this.dbManager.getActiveDB() == null)   {
            throw new IOException("Erreur : Aucune base de données active.");
        }
        String[] splitCommand = texteCommande.split(" ");
        if (splitCommand.length==4) {
            String tableName = splitCommand[2].toUpperCase();
            if (! this.dbManager.getActiveDB().containsTable(tableName))  {
                throw new IOException("Erreur : la table (" + tableName + ") n'existe pas dans la base de données actuellement active {" + this.dbManager.getActiveDB().getNom() + "}");
            }
            if (! splitCommand[3].endsWith(".csv"))   {
                throw new IOException("Erreur : le fichier en entrée doit être un fichier CSV d'extention .csv");
            }
            File file = new File ("./src/data/"+splitCommand[3]);
            if (!file.exists())  {
                throw new IOException("Erreur : le fichier en entrée n'existe pas dans l'arborescence \"./src/data\" de ce projet.");
            }
            try {
                FileReader fileReader = new FileReader(file);
                BufferedReader buffer = new BufferedReader(fileReader);
                String line = null;
                while ((line=buffer.readLine()) != null)    {
                    String query = "INSERT INTO " + tableName + " VALUES (" + line + ")";
                    this.ProcessInsertIntoCommand(query); 
                }
                buffer.close();
            }   catch(FileNotFoundException e)  {
                throw new IOException("Erreur : tentative d'ouverture du fichier ("+ splitCommand[3] + ") échouée !");
            }
        }   else    {
            throw new IOException("Erreur de syntaxe dans la commande BULKINSERT INTO. Syntaxe à suivre : \"BULKINSERT INTO X Y.csv\noù X est remplacé par le nom de la table; et Y correspondant au nom du fichier qui contient les records, le fichier doit être placé dans le dossier \"/src/data\" du projet.");
        }
    }

    public void ProcessQuitCommand (String texteCommande)   {
        this.bufferManager.flushBuffers();
        this.diskManager.saveState();
        this.dbManager.SaveState();
    }

    public void ProcessSelectCommand(String textecommande)  throws IOException  {
        String [] repartition1 = textecommande.toUpperCase().split(" ");
        if (repartition1.length<5  ||  !repartition1[2].equals("FROM"))   {
            throw new IOException("Erreur : Erreur de syntaxe dans la commande SELECT. La commande doit obligatoirement contenir les mots \"SELECT et FROM\" en troisième position.");
        }
        if (repartition1[4].contains(","))  {
            this.processSelectJointureCommand(textecommande);
            return;
        }
        String tableName = repartition1[3];
        if (! this.dbManager.getActiveDB().containsTable(tableName))  {
            throw new IOException("Erreur : la table (" + tableName + ") n'existe pas dans la base de données actuellement active {" + this.dbManager.getActiveDB().getNom() + "}");
        }
        Relation table = this.dbManager.getActiveDB().getTable(tableName);
        String tableAlias = repartition1[4];

        ArrayList<Integer> projectionIndexs = new ArrayList<>();
        if (repartition1[1].equals("*"))    {
            for (int i=0; i<table.getNbColonnes(); i+=1)    {
                projectionIndexs.add(i);
            }
        }   else    {
            String [] repartition2 = repartition1[1].split(",");
            if (repartition2.length>table.getNbColonnes()) {
                throw new IOException("Erreur : Erreur de syntaxe dans la commande SELECT dans la partie \"" + textecommande.split(" ")[1] + "\", consultez la documentation pour en savoir plus.");
            }
            for (int i=0; i<repartition2.length; i+=1)  {
                String[] repartition3 = repartition2[i].split("\\.");
                if (repartition3.length != 2)   {
                    throw new IOException("Erreur : Erreur de syntaxe dans la commande SELECT dans la partie \"" + textecommande.split(" ")[1] + "\", consultez la documentation pour en savoir plus.");
                }
                if (!repartition3[0].equals(tableAlias))    {
                    throw new IOException("Erreur : Erreur de syntaxe dans la commande SELECT dans la partie \"" + textecommande.split(" ")[1] + "\", Pensez à garder le même alias pour la table durant toute la requête.");
                }
                String attributeName = repartition3[1].toLowerCase();
                String indexAndTypeOfAttribute = table.getIndexAndTypeOfAttribute(attributeName);
                if (indexAndTypeOfAttribute.equals("")) {
                    throw new IOException("Erreur : Erreur de syntaxe dans la commande SELECT, la table (" + tableName + ") ne contient aucun attribut s'appelant \"" + attributeName + "\" !!");
                }
                projectionIndexs.add(Integer.parseInt(indexAndTypeOfAttribute.split(";")[0]));
            }
        }

        ArrayList<Condition> conditions = new ArrayList<>();
        if (repartition1.length>5)  {
            if (!repartition1[5].equals("WHERE"))   {
                throw new IOException("Erreur : Erreur de syntaxe dans la commande SELECT, la position 6 de cette commande est résérvée pour le terme WHERE pour exprimer une condition.");
            }
            String where = textecommande.split(" ")[5];
            String [] repartition4 = textecommande.split(where);
            if (repartition4.length!=2)    {
                throw new IOException("Erreur : Erreur de syntaxe dans la commande SELECT, la requête doit contenir au minimum une seule condition après \"WHERE\" !");
            }
            repartition4 = repartition4[1].trim().split(" ");
            for (int i=0; i<repartition4.length; i+=1)     {
                if (i%2==0) {
                    String conditionString = repartition4[i];
                    String operateur = "";
                    if (conditionString.contains("!=")) {
                        operateur = "!=";
                    }   else if (conditionString.contains("<="))  {
                        operateur = "<=";
                    }   else if (conditionString.contains(">="))    {
                        operateur = ">=";
                    }   else if (conditionString.contains("=")) {
                        operateur = "=";
                    }   else if (conditionString.contains("<")) {
                        operateur = "<";
                    }   else if (conditionString.contains(">")) {
                        operateur = ">";
                    }
                    if (operateur.equals(""))  {
                        throw new IOException("Erreur : Erreur de syntaxe dans la commande SELECT dans la partie \"" + conditionString + "\", les seuls opérateurs autorisés sont :  =  !=  <  >  <=  >=");
                    }

                    String [] repartition5 = conditionString.split(operateur);
                    if (repartition5.length!=2) {
                        throw new IOException("Erreur : Erreur de syntaxe dans la commande SELECT dans la partie \"" + conditionString + "\", veuillez regarder la documentation..!");
                    }
                    String leftPart = repartition5[0].trim(); 
                    String rightPart = repartition5[1].trim();

                    // Déterminer si une inversion est nécessaire
                    boolean leftIsColumn = leftPart.contains(".") && !this.isDouble(leftPart);
                    boolean rightIsColumn = rightPart.contains(".") && !isDouble(rightPart);

                    boolean shouldInvert = !leftIsColumn && rightIsColumn;

                    // Si nécessaire, inverser la condition
                    if (shouldInvert) {
                        repartition5[0] = rightPart ;
                        repartition5[1] = leftPart;
                        operateur = Operateur.inverseOperateur(operateur);
                    }

                    String [] repartition6 = repartition5[0].toUpperCase().split("\\.");
                    if (repartition6.length!=2)   {
                        throw new IOException("Erreur : Erreur de syntaxe dans la commande SELECT dans la partie \"" + conditionString + "\", veuillez regarder la documentation..!");
                    }
                    if (!repartition6[0].equals(tableAlias))    {
                        throw new IOException("Erreur : Erreur de syntaxe dans la commande SELECT dans la partie \"" + conditionString + "\", Pensez à garder le même alias pour la table durant toute la requête.");
                    }
                    String indexAndType = table.getIndexAndTypeOfAttribute(repartition6[1].toLowerCase());
                    String valeurConstante = repartition5[1];

                    if (valeurConstante.split("\\.")[0].equalsIgnoreCase(tableAlias)) {
                        String indexAndType2 = table.getIndexAndTypeOfAttribute(valeurConstante.split("\\.")[1].toLowerCase());
                        valeurConstante="#.##.#"+indexAndType2.split(";")[0];
                    }   else if (indexAndType.endsWith("CHAR") || indexAndType.endsWith("VARCHAR"))  {
                        if (!(valeurConstante.startsWith("\"") && valeurConstante.endsWith("\"")))  {
                            throw new IOException("Erreur : Erreur de syntaxe dans la commande SELECT dans la partie \"" + conditionString + "\", Pensez à mettre les constantes CHAR et VARCHAR entre guillemets \""+valeurConstante+"\".");
                        }
                        valeurConstante = valeurConstante.substring(1,valeurConstante.length()-1);
                    }   else if (indexAndType.endsWith("INT"))  {
                        try {
                            Integer.parseInt(valeurConstante);
                        }   catch (NumberFormatException e) {
                                throw new IOException("Erreur : Erreur de syntaxe dans la commande SELECT dans la partie \"" + conditionString + "\", Pensez à lire la documentation.");
                        }
                    }   else if (indexAndType.endsWith("REAL"))    {
                        try {
                            Double.parseDouble(valeurConstante);
                        }   catch (NumberFormatException e) {
                                throw new IOException("Erreur : Erreur de syntaxe dans la commande SELECT dans la partie \"" + conditionString + "\", Pensez à lire la documentation.");
                        }
                    }

                    
                    try{Integer.parseInt(indexAndType.split(";")[0]);} catch(NumberFormatException e){throw new IOException("Erreur de syntaxe : l'attribut " + repartition6[1] + " n'existe pas dans la table (" + tableName + ") !");}
                    Condition condition = new Condition(valeurConstante,   Integer.parseInt(indexAndType.split(";")[0]) , operateur, indexAndType.split(";")[1]);
                    conditions.add(condition);

                }   else if (!repartition4[i].toUpperCase().equals("AND"))  {
                    throw new IOException("Erreur : le seul operateur possible entre les conditions est l'opérateur logique de conjenction AND !");
                }
            }
        } 

        RecordPrinter recordPrinter = new RecordPrinter(table, conditions, projectionIndexs); 

        recordPrinter.printRecords();
    }

    public boolean isDouble(String str) {
        if (str == null || str.isEmpty()) {
            return false; // Une chaîne nulle ou vide ne peut pas être un double
        }
        try {
            Double.parseDouble(str);
            return true; // La conversion a réussi, c'est un double
        } catch (NumberFormatException e) {
            return false; // La conversion a échoué, ce n'est pas un double
        }
    }



    public void processSelectJointureCommand(String texteCommande)    throws IOException{
        String [] repartition1 = texteCommande.toUpperCase().split(" ");
        if (repartition1.length<8  ||  !repartition1[2].equals("FROM")  ||  !repartition1[6].equals("WHERE"))   {  
            throw new IOException("Erreur : Erreur de syntaxe dans la commande SELECT. La commande doit obligatoirement contenir les mots \"SELECT et FROM et Where\" en première, troisième et septième position.");
        }
        String table1Name = repartition1[3];
        if (! this.dbManager.getActiveDB().containsTable(table1Name))  {
            throw new IOException("Erreur : la table (" + table1Name + ") n'existe pas dans la base de données actuellement active {" + this.dbManager.getActiveDB().getNom() + "}");
        }
        Relation table1 = this.dbManager.getActiveDB().getTable(table1Name);
        String[] repartition2 = repartition1[4].split(",");
        if (repartition2.length!=2) {
            throw new IOException("Erreur : Erreur de syntaxe dans la partie \""+ repartition1[4] + "\" !!");
        }
        String table1Alias = repartition2[0];
        String table2Name = repartition2[1];
        if (! this.dbManager.getActiveDB().containsTable(table2Name))  {
            throw new IOException("Erreur : la table (" + table2Name + ") n'existe pas dans la base de données actuellement active {" + this.dbManager.getActiveDB().getNom() + "}");
        }
        Relation table2 = this.dbManager.getActiveDB().getTable(table2Name);
        String table2Alias = repartition1[5];

        ArrayList<Condition> conditions = new ArrayList<>();

        if (!repartition1[6].equals("WHERE"))   {
            throw new IOException("Erreur : Erreur de syntaxe dans la commande SELECT, la position 6 de cette commande est résérvée pour le terme WHERE pour exprimer une condition.");
        }
        String where = texteCommande.split(" ")[6];
        String [] repartition4 = texteCommande.split(where);
        if (repartition4.length!=2)    {
            throw new IOException("Erreur : Erreur de syntaxe dans la commande SELECT, la requête doit contenir au minimum une seule condition après \"WHERE\" !");
        }
        repartition4 = repartition4[1].trim().split(" ");
        ArrayList<Integer> indexsConditions = new ArrayList<>();
        for (int i=0; i<repartition4.length; i+=1)     {
            if (i%2==0) {
                String conditionString = repartition4[i];
                String operateur = "";
                if (conditionString.contains("!=")) {
                    operateur = "!=";
                }   else if (conditionString.contains("<="))  {
                    operateur = "<=";
                }   else if (conditionString.contains(">="))    {
                    operateur = ">=";
                }   else if (conditionString.contains("=")) {
                    operateur = "=";
                }   else if (conditionString.contains("<")) {
                    operateur = "<";
                }   else if (conditionString.contains(">")) {
                    operateur = ">";
                }
                if (operateur.equals(""))  {
                    throw new IOException("Erreur : Erreur de syntaxe dans la commande SELECT dans la partie \"" + conditionString + "\", les seuls opérateurs autorisés sont :  =  !=  <  >  <=  >=");
                }
                String [] repartition5 = conditionString.split(operateur);
                if (repartition5.length!=2) {
                    throw new IOException("Erreur : Erreur de syntaxe dans la commande SELECT dans la partie \"" + conditionString + "\", veuillez regarder la documentation..!");
                }
                
                String [] repartition6 = repartition5[0].toUpperCase().split("\\.");
                if (repartition6.length!=2)   {
                    throw new IOException("Erreur : Erreur de syntaxe dans la commande SELECT dans la partie \"" + conditionString + "\", veuillez regarder la documentation..!");
                }
                if (!repartition6[0].equals(table1Alias) && !repartition6[0].equals(table2Alias))    {
                    throw new IOException("Erreur : Erreur de syntaxe dans la commande SELECT dans la partie \"" + conditionString + "\", Pensez à garder le même alias pour la table durant toute la requête.");
                }
                String indexAndType = null;
                if (repartition6[0].equals(table1Alias))
                    indexAndType = table1.getIndexAndTypeOfAttribute(repartition6[1].toLowerCase());
                else 
                    indexAndType = table2.getIndexAndTypeOfAttribute(repartition6[1].toLowerCase());
                 
                repartition6 = repartition5[1].toUpperCase().split("\\.");
                if (repartition6.length!=2)   {
                    throw new IOException("Erreur : Erreur de syntaxe dans la commande SELECT dans la partie \"" + conditionString + "\", veuillez regarder la documentation..!");
                }
                if (!repartition6[0].equals(table1Alias) && !repartition6[0].equals(table2Alias))    {
                    throw new IOException("Erreur yy : Erreur de syntaxe dans la commande SELECT dans la partie \"" + conditionString + "\", Pensez à garder le même alias pour la table durant toute la requête.");
                }
                String indexAndType2 = null;
                if (repartition6[0].equals(table1Alias))
                    indexAndType2 = table1.getIndexAndTypeOfAttribute(repartition6[1].toLowerCase());
                else 
                    indexAndType2 = table2.getIndexAndTypeOfAttribute(repartition6[1].toLowerCase());

                if (repartition6[0].equals(table1Alias))    {
                    String aux = indexAndType;
                    indexAndType = indexAndType2;
                    indexAndType2 = aux;
                }                

                indexsConditions.add(Integer.parseInt(indexAndType.split(";")[0]));
                Condition condition = new Condition("", Integer.parseInt(indexAndType2.split(";")[0]), operateur, indexAndType2.split(";")[1]);
                conditions.add(condition);
            }   else if (!repartition4[i].toUpperCase().equals("AND"))  {
                throw new IOException("Erreur : le seul operateur possible entre les conditions est l'opérateur logique de conjenction AND !");
            }
        }

        PageDirectoryIterator table1IteratorDataPages = new PageDirectoryIterator(table1);
        PageDirectoryIterator table2IteratorDataPages = new PageDirectoryIterator(table2);
        ArrayList<Record> records = new ArrayList<>();
        PageId dataPageTable1 = null;

        
        while ((dataPageTable1=table1IteratorDataPages.GetNextDataPageId()) != null)     {
            DataPageHoldRecordIterator table1IteratorRecords = new DataPageHoldRecordIterator(dataPageTable1, table1);
            Record table1ActualRecord = null;
            while ((table1ActualRecord=table1IteratorRecords.getNextRecord()) != null)  {
                int x=0;
                for (Condition condition: conditions)   {
                    int index = indexsConditions.get(x); x+=1;
                    String valeurConstante = table1ActualRecord.getAttributs()[index];
                    condition.setValeurConstante(valeurConstante);
                }
                ArrayList<Integer> proj = new ArrayList<>(); 
                for (int i=0; i<table2.getNbColonnes(); i+=1)   
                    proj.add(i);
                RecordPrinter recordPrinter = new RecordPrinter(table2, conditions, proj); 
                ArrayList<Record> recordsTemporaires = recordPrinter.loadRecords();
                if (recordsTemporaires.size()!=0)   {
                    for (int j=0 ; j<recordsTemporaires.size(); j+=1)   {
                        String [] attributsRecord = new String[table1.getNbColonnes() + table2.getNbColonnes()];
                        for (int i=0; i<attributsRecord.length; i+=1)   {
                            if (i<table1.getNbColonnes())   {
                                attributsRecord[i] = table1ActualRecord.getAttributs()[i];
                            }   else {
                                attributsRecord[i] = recordsTemporaires.get(j).getAttributs()[i-table1.getNbColonnes()];
                            }
                        }
                        records.add(new Record(attributsRecord));
                    }
                }                
            }
            table1IteratorRecords.close();
        }

        for (int i=0 ; i<table1.getNbColonnes() + table2.getNbColonnes() ; i+=1)   {
            if (i<table1.getNbColonnes()) {
                System.out.print(table1.getColonnes()[i].getNom()  +  " , ");
            }   else {
                System.out.print(table2.getColonnes()[i-table1.getNbColonnes()].getNom()  +  ((i!=table1.getNbColonnes() + table2.getNbColonnes()-1) ? " , " : " .\n"));
            }
        }
        for (Record record : records) {
            System.out.println(record);
        }
        System.out.println("Total Records = " + records.size());




    }


    public static void main (String [] args)    {
        DBConfig config = DBConfig.loadDBConfig("config.json");
        SGBD mySGBD = new SGBD (config);
        mySGBD.Run();
    }
}

    