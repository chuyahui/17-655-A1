Imports System.IO

''' <summary>
''' This is the UI for A1.
''' </summary>
''' <remarks></remarks>

Public Class Form1
    ''' Create a new MyUtilities class
    Private myUtil As New MyUtilities
    Private Sub Form1_Load(sender As Object, e As EventArgs) Handles MyBase.Load
        Default_Value(FindCurrentFolder)
    End Sub

    ''The default value for data and java location is the distribution folder
    Private Sub Default_Value(ByVal DistriubtionFolderPath As String)
        txtDataLocation.Text = DistriubtionFolderPath
    End Sub
    ''Run SyStem A
    Private Sub btSystemA_Click(sender As Object, e As EventArgs) Handles btSystemA.Click
        ''Run System A
        RunSystem("SystemA")
        ''Write the result in the result box
        Call Open_OutPutA()
    End Sub

    ''Run System B 
    Private Sub BtSystemB_Click(sender As Object, e As EventArgs) Handles BtSystemB.Click
        ''Run System B
        RunSystem("SystemB")
        ''Write the result in the result box
        Call Open_OutPutB()
    End Sub
    ''Run System C
    Private Sub BtSystemC_Click(sender As Object, e As EventArgs) Handles BtSystemC.Click
        ''Run System C
        RunSystem("SystemC")
        ''Write the result in the result box
        Call Open_OutPutCLess_Than10K()
        Call Open_OutPutCWildPoints()
    End Sub
    Private Sub RunSystem(ByVal System_Name As String)
        Try
            ''Clean Result
            ResultBlock.Clear()
            ''Create value to hold time value
            Static start_time As DateTime
            Static stop_time As DateTime
            Dim elapsed_time As TimeSpan
            ''Time start
            start_time = Now
            ''Run system A
            myUtil.runSystem(System_Name & ".jar", txtDataLocation.Text)
            ''Time Stop
            stop_time = Now
            ''Calc elapsed Time
            elapsed_time = stop_time.Subtract(start_time)
            ''Display elapsed Time in the status text box
            txtStatus.Text = System_Name & " Time(Seconds):" & elapsed_time.TotalSeconds.ToString("0.000000")
        Catch ex As Exception
            txtStatus.Text = "Unable to run " & System_Name
        End Try
    End Sub
    ''FindCurrentFolder
    Private Function FindCurrentFolder() As String
        Dim path As String = Directory.GetCurrentDirectory()
        Return path
    End Function
    ''FindParentFolder
    Private Function FindParentFolder() As String
        Dim path As String = Directory.GetCurrentDirectory()
        Dim parentPath As String = GetParent(path)
        Return parentPath
    End Function
    ''GetParent
    Private Function GetParent(ByVal path As String) As String
        Try
            Dim directoryInfo As System.IO.DirectoryInfo
            directoryInfo = System.IO.Directory.GetParent(path)
            Return directoryInfo.ToString
        Catch exp As ArgumentNullException
            System.Console.WriteLine("Path is a null reference.")
            Return "Nothing"
        Catch exp As ArgumentException
            System.Console.WriteLine("Path is an empty string, " + _
                                 "contains only white spaces, or " + _
                                 "contains invalid characters.")
            Return "Nothing"
        End Try
    End Function
    ''Open_OutPutA & add header
    Private Sub Open_OutPutA()
        ResultBlock.Text = ResultBlock.Text & vbNewLine
        ResultBlock.Text = ResultBlock.Text & "Time:                      Temperature (C):       Altitude (m):" & vbNewLine
        ResultBlock.Text = ResultBlock.Text & myUtil.readFile(txtDataLocation.Text & "\OutputA.dat")
    End Sub
    ''Open_OutPutB & add header
    Private Sub Open_OutPutB()
        ResultBlock.Text = ResultBlock.Text & vbNewLine
        ResultBlock.Text = ResultBlock.Text & "Time:                      Temperature (C):       Altitude (m):              Pressure (psi):" & vbNewLine
        ResultBlock.Text = ResultBlock.Text & myUtil.readFile(txtDataLocation.Text & "\OutputB.dat")
    End Sub
    ''Open_OutPutC Less_Then10K & add header
    Private Sub Open_OutPutCLess_Than10K()
        ResultBlock.Text = ResultBlock.Text & vbNewLine
        ResultBlock.Text = ResultBlock.Text & "Time:                      Altitude (m):" & vbNewLine
        ResultBlock.Text = ResultBlock.Text & myUtil.readFile(txtDataLocation.Text & "\LessThan10K.dat")
    End Sub
    ''Open_OutPutC wild Points & add header
    Private Sub Open_OutPutCWildPoints()
        ResultBlock.Text = ResultBlock.Text & vbNewLine
        ResultBlock.Text = ResultBlock.Text & "Time:                      Pressure (psi):" & vbNewLine
        ResultBlock.Text = ResultBlock.Text & myUtil.readFile(txtDataLocation.Text & "\PressureWildPoints.dat")
    End Sub
    ''Clean Result
    Private Sub btClean_Click(sender As Object, e As EventArgs) Handles btClean.Click
        ResultBlock.Clear()
    End Sub
End Class

''Utilities class 
Public Class MyUtilities
    ''Run Single RunCommand
    Public Sub RunCommandCom(command As String, arguments As String, permanent As Boolean)
        Dim p As Process = New Process()
        Dim pi As ProcessStartInfo = New ProcessStartInfo()
        pi.Arguments = " " + If(permanent = True, "/K", "/C") + " " + command + " " + arguments
        pi.FileName = "cmd.exe"
        p.StartInfo = pi
        p.Start()
    End Sub
    ''Read Hex
    Public Function ReadHex(ByVal FILE_NAME As String) As String
        Dim bytes As Byte() = IO.File.ReadAllBytes(FILE_NAME)
        Dim hex As String() = Array.ConvertAll(bytes, Function(b) b.ToString("X2"))

        Return String.Join(" ", hex)
    End Function
    ''Read File using streamReader
    Public Function readFile(ByVal FILE_NAME As String) As String
        Dim line As String = ""
        Try
            ' Open the file using a stream reader.
            Using sr As New StreamReader(FILE_NAME)
                ' Read the stream to a string and write the string to the console.
                Do While sr.Peek() <> -1
                    line = line & sr.ReadLine() & vbNewLine
                Loop

            End Using
        Catch e As Exception
            Console.WriteLine("The file could not be read:")
            Console.WriteLine("The file could not be read:" & e.Message)
            line = e.Message
        End Try
        Return line
    End Function
    ''Run Java file
    Public Sub runSystem(ByVal System As String, ByVal DistributionLocation As String)
        Using p1 As New Process
            p1.StartInfo.FileName = "cmd.exe"
            p1.StartInfo.Arguments = "/C CD " & DistributionLocation & " & java -jar " & System & " " & DistributionLocation
            Try
                p1.Start()
            Catch ex As Exception
                MessageBox.Show(ex.Message)
            End Try
            p1.WaitForExit()
        End Using
    End Sub
    ''Open File 
    Public Sub OpenFile(ByVal FILE_NAME As String)
        If System.IO.File.Exists(FILE_NAME) = True Then
            Process.Start(FILE_NAME)
        Else
            MsgBox("File Does Not Exist. Path:" & FILE_NAME)
        End If
    End Sub
End Class