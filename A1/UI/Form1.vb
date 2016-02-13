Imports System.IO

Public Class Form1
    Private myUtil As New MyUtilities
    Private Sub Default_Value(ByVal DistriubtionFolderPath As String)
        txtSysA.Text = "java -jar SystemA.jar " & DistriubtionFolderPath
        txtSysB.Text = "java -jar SystemB.jar " & DistriubtionFolderPath
        txtSysC.Text = "java -jar SystemC.jar " & DistriubtionFolderPath
        txtDataLocation.Text = DistriubtionFolderPath
    End Sub
    Private Sub btSystemA_Click(sender As Object, e As EventArgs) Handles btSystemA.Click
        Static start_time As DateTime
        Static stop_time As DateTime
        Dim elapsed_time As TimeSpan

        start_time = Now
        myUtil.runSystem("SystemA.jar", txtDataLocation.Text)
        stop_time = Now
        elapsed_time = stop_time.Subtract(start_time)
        ResultBlock.Text = "SystemA Time(Seconds):" & elapsed_time.TotalSeconds.ToString("0.000000")
    End Sub

    Private Sub BtSystemB_Click(sender As Object, e As EventArgs) Handles BtSystemB.Click
        Static start_time As DateTime
        Static stop_time As DateTime
        Dim elapsed_time As TimeSpan
        start_time = Now
        myUtil.runSystem("SystemB.jar", txtDataLocation.Text)
        stop_time = Now
        elapsed_time = stop_time.Subtract(start_time)
        ResultBlock.Text = "SystemB Time (Seconds): " & elapsed_time.TotalSeconds.ToString("0.000000")
    End Sub

    Private Sub BtSystemC_Click(sender As Object, e As EventArgs) Handles BtSystemC.Click
        Static start_time As DateTime
        Static stop_time As DateTime
        Dim elapsed_time As TimeSpan
        start_time = Now
        myUtil.runSystem("SystemC.jar", txtDataLocation.Text)
        stop_time = Now
        elapsed_time = stop_time.Subtract(start_time)
        ResultBlock.Text = "SystemC Time(Seconds):" & elapsed_time.TotalSeconds.ToString("0.000000")
    End Sub

    Private Function FindCurrentFolder() As String
        Dim path As String = Directory.GetCurrentDirectory()
        Return path
    End Function

    Private Function FindParentFolder() As String
        Dim path As String = Directory.GetCurrentDirectory()
        Dim parentPath As String = GetParent(path)
        Return parentPath
    End Function


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


    Private Sub Form1_Load(sender As Object, e As EventArgs) Handles MyBase.Load
        Default_Value(FindCurrentFolder)
    End Sub

    Private Sub btOpenA_Click(sender As Object, e As EventArgs) Handles btOpenA.Click
        ResultBlock.Text = "Time:                      Temperature (C):       Altitude (m):" & vbNewLine
        ResultBlock.Text = ResultBlock.Text & myUtil.readFile(txtDataLocation.Text & "\OutputA.dat")
    End Sub

    Private Sub btOpenB_Click(sender As Object, e As EventArgs) Handles btOpenB.Click
        ResultBlock.Text = "Time:                      Temperature (C):       Altitude (m):              Pressure (psi):" & vbNewLine
        ResultBlock.Text = ResultBlock.Text & myUtil.readFile(txtDataLocation.Text & "\OutputB.dat")
    End Sub
    Private Sub btOpenC_Click(sender As Object, e As EventArgs) Handles btOpenC.Click
        ResultBlock.Text = "Time:                      Altitude (m):" & vbNewLine
        ResultBlock.Text = ResultBlock.Text & myUtil.readFile(txtDataLocation.Text & "\LessThan10K.dat")
    End Sub
    Private Sub btOpenC_Wild_Click(sender As Object, e As EventArgs) Handles btOpenC_Wild.Click
        ResultBlock.Text = "Time:                      Pressure (psi):" & vbNewLine
        ResultBlock.Text = ResultBlock.Text & myUtil.readFile(txtDataLocation.Text & "\PressureWildPoints.dat")
    End Sub


    Private Sub txtDataLocation_TextChanged(sender As Object, e As EventArgs) Handles txtDataLocation.TextChanged
        Default_Value(txtDataLocation.Text)
    End Sub

    Private Sub btClean_Click(sender As Object, e As EventArgs) Handles btClean.Click
        ResultBlock.Clear()
    End Sub
    Private Sub btOpenFlightData_Click(sender As Object, e As EventArgs) Handles btOpenFlightData.Click
        ResultBlock.Text = myUtil.readFile(txtDataLocation.Text & "\SourceFile\FlightData_Orig.txt")
    End Sub

    Private Sub btOpenSubA_Click(sender As Object, e As EventArgs) Handles btOpenSubA.Click
        ResultBlock.Text = myUtil.readFile(txtDataLocation.Text & "\SourceFile\SubSetA_Orig.txt")
    End Sub

    Private Sub Button1_Click(sender As Object, e As EventArgs) Handles Button1.Click
        ResultBlock.Text = myUtil.readFile(txtDataLocation.Text & "\SourceFile\SubSetB_Orig.txt")
    End Sub
End Class

Public Class MyUtilities
    Public Sub RunCommandCom(command As String, arguments As String, permanent As Boolean)
        Dim p As Process = New Process()
        Dim pi As ProcessStartInfo = New ProcessStartInfo()
        pi.Arguments = " " + If(permanent = True, "/K", "/C") + " " + command + " " + arguments
        pi.FileName = "cmd.exe"
        p.StartInfo = pi
        p.Start()
    End Sub

    Public Function ReadHex(ByVal FILE_NAME As String) As String
        Dim bytes As Byte() = IO.File.ReadAllBytes(FILE_NAME)
        Dim hex As String() = Array.ConvertAll(bytes, Function(b) b.ToString("X2"))

        Return String.Join(" ", hex)
    End Function

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

    Public Sub OpenFile(ByVal FILE_NAME As String)
        If System.IO.File.Exists(FILE_NAME) = True Then
            Process.Start(FILE_NAME)
        Else
            MsgBox("File Does Not Exist. Path:" & FILE_NAME)
        End If
    End Sub
End Class