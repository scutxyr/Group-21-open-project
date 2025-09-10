import pyodbc

class SQLServerConnection(object):
    def __init__(self, server, database, username, password):
        self.server = server
        self.database = database
        self.username = username
        self.password = password
        self.connection = None
        self.cursor = None
        self.connected = False

    def test_version(self):
        try:
            for x in pyodbc.drivers():
                if x.startswith('Microsoft Access Driver'):
                    print(x)
        except pyodbc.Error as e:
            pass



    def connect(self):
        try:
            connection_string = (
                f"DRIVER={{ODBC Driver 13 for SQL Server}};"
                f"SERVER={self.server};"
                f"DATABASE={self.database};"
                f"UID={self.username};"
                f"PWD={self.password}"
            )
            self.connection = pyodbc.connect(connection_string)
            self.cursor = self.connection.cursor()
            self.connected = True
            print("Connected to SQL Server")
        except pyodbc.Error as e:
            print(f"Error connecting to SQL Server: {e}")
            self.connected = False

    def disconnect(self):
        if self.cursor:
            self.cursor.close()
        if self.connection:
            self.connection.close()
        self.connected = False
        print("Disconnected from SQL Server")

    def execute_query(self, query):
        if not self.connected:
            raise Exception("Not connected to the database")
        if self.validate_query(query):
            try:
                self.cursor.execute(query)
                print(f"Executed query: {query}")
                return self.fetch_results()
            except pyodbc.Error as e:
                print(f"Error executing query: {e}")

    def fetch_results(self):
        if not self.connected:
            raise Exception("Not connected to the database")
        try:
            if self.cursor.description is None:
                raise Exception("No query executed before fetching results")
            results = self.cursor.fetchall()
            print("Fetched results")
            return results
        except pyodbc.Error as e:
            print(f"Error fetching results: {e}")
            self.connection.rollback()
            return []

    @staticmethod
    def validate_query(query):
        upper_query = query.strip().upper()
        if not upper_query.startswith("SELECT"):
            raise ValueError("Only SELECT queries are allowed.")
        if any(keyword in upper_query for keyword in ["UPDATE", "DELETE", "INSERT", "DROP", "ALTER", "CREATE"]):
            raise ValueError("Query contains forbidden keywords.")
        return True