Add-Type -AssemblyName PresentationFramework, PresentationCore, WindowsBase

$adbPath = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
$projectDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$defaultApk = Join-Path $projectDir "app\build\outputs\apk\debug\app-debug.apk"

[xml]$xaml = @"
<Window xmlns="http://schemas.microsoft.com/winfx/2006/xaml/presentation"
        xmlns:x="http://schemas.microsoft.com/winfx/2006/xaml"
        Title="Pixel (Un)Fold APK Deployer" Height="580" Width="680"
        Background="#121316" WindowStartupLocation="CenterScreen"
        ResizeMode="CanMinimize" FontFamily="Segoe UI">
    <Window.Resources>
        <Style TargetType="Button">
            <Setter Property="Background" Value="#1E4589"/>
            <Setter Property="Foreground" Value="#D3E3FD"/>
            <Setter Property="FontSize" Value="14"/>
            <Setter Property="FontWeight" Value="SemiBold"/>
            <Setter Property="BorderThickness" Value="0"/>
            <Setter Property="Padding" Value="14,10"/>
            <Setter Property="Cursor" Value="Hand"/>
            <Setter Property="Template">
                <Setter.Value>
                    <ControlTemplate TargetType="Button">
                        <Border Background="{TemplateBinding Background}" CornerRadius="8" Padding="{TemplateBinding Padding}">
                            <ContentPresenter HorizontalAlignment="Center" VerticalAlignment="Center"/>
                        </Border>
                    </ControlTemplate>
                </Setter.Value>
            </Setter>
            <Style.Triggers>
                <Trigger Property="IsMouseOver" Value="True">
                    <Setter Property="Background" Value="#2A5BB2"/>
                </Trigger>
            </Style.Triggers>
        </Style>
    </Window.Resources>

    <Grid Margin="20">
        <Grid.RowDefinitions>
            <RowDefinition Height="Auto"/>
            <RowDefinition Height="Auto"/>
            <RowDefinition Height="Auto"/>
            <RowDefinition Height="*"/>
        </Grid.RowDefinitions>

        <!-- Header -->
        <StackPanel Grid.Row="0" Margin="0,0,0,16">
            <TextBlock Text="Pixel (Un)Fold APK Deployer" FontSize="24" FontWeight="Bold" Foreground="#FFFFFF"/>
            <TextBlock Text="One-click manual APK installer for Pixel Fold &amp; Android devices" FontSize="13" Foreground="#8C8E96" Margin="0,2,0,0"/>
        </StackPanel>

        <!-- Device Status Card -->
        <Border Grid.Row="1" Background="#1B1D22" CornerRadius="12" BorderBrush="#2C2F36" BorderThickness="1" Padding="16" Margin="0,0,0,16">
            <Grid>
                <Grid.ColumnDefinitions>
                    <ColumnDefinition Width="*"/>
                    <ColumnDefinition Width="Auto"/>
                </Grid.ColumnDefinitions>
                <StackPanel Grid.Column="0">
                    <TextBlock Text="CONNECTED DEVICE" FontSize="11" FontWeight="Bold" Foreground="#8C8E96" Margin="0,0,0,4"/>
                    <TextBlock x:Name="TxtDeviceName" Text="Checking for connected device..." FontSize="16" FontWeight="SemiBold" Foreground="#A8C7FA"/>
                    <TextBlock x:Name="TxtDeviceInfo" Text="Searching ADB..." FontSize="12" Foreground="#8C8E96" Margin="0,2,0,0"/>
                </StackPanel>
                <Button x:Name="BtnRefresh" Grid.Column="1" Content="Refresh" Background="#262A33" Foreground="#A8C7FA" Padding="12,6" VerticalAlignment="Center"/>
            </Grid>
        </Border>

        <!-- Actions Card -->
        <Border Grid.Row="2" Background="#1B1D22" CornerRadius="12" BorderBrush="#2C2F36" BorderThickness="1" Padding="16" Margin="0,0,0,16">
            <StackPanel>
                <TextBlock Text="DEPLOYMENT ACTIONS" FontSize="11" FontWeight="Bold" Foreground="#8C8E96" Margin="0,0,0,12"/>
                <Grid>
                    <Grid.ColumnDefinitions>
                        <ColumnDefinition Width="*"/>
                        <ColumnDefinition Width="12"/>
                        <ColumnDefinition Width="*"/>
                    </Grid.ColumnDefinitions>
                    <Button x:Name="BtnDeployLatest" Grid.Column="0" Content="Deploy Latest Pixel (Un)Fold APK" Background="#34A853" Foreground="#FFFFFF"/>
                    <Button x:Name="BtnPickApk" Grid.Column="2" Content="Browse &amp; Push Any .APK..." Background="#1E4589" Foreground="#D3E3FD"/>
                </Grid>

                <Grid Margin="0,10,0,0">
                    <Grid.ColumnDefinitions>
                        <ColumnDefinition Width="*"/>
                        <ColumnDefinition Width="12"/>
                        <ColumnDefinition Width="*"/>
                    </Grid.ColumnDefinitions>
                    <Button x:Name="BtnLaunchApp" Grid.Column="0" Content="Launch App on Phone" Background="#262A33" Foreground="#E2E2E6" Padding="10,8" FontSize="13"/>
                    <Button x:Name="BtnScreenshot" Grid.Column="2" Content="Capture Phone Screenshot" Background="#262A33" Foreground="#E2E2E6" Padding="10,8" FontSize="13"/>
                </Grid>
            </StackPanel>
        </Border>

        <!-- Output Log Box -->
        <Border Grid.Row="3" Background="#000000" CornerRadius="10" BorderBrush="#26282E" BorderThickness="1" Padding="12">
            <Grid>
                <Grid.RowDefinitions>
                    <RowDefinition Height="Auto"/>
                    <RowDefinition Height="*"/>
                </Grid.RowDefinitions>
                <TextBlock Grid.Row="0" Text="CONSOLE OUTPUT" FontSize="10" FontWeight="Bold" Foreground="#5E626E" Margin="0,0,0,6"/>
                <ScrollViewer Grid.Row="1" VerticalScrollBarVisibility="Auto">
                    <TextBox x:Name="TxtLogs" TextWrapping="Wrap" Background="Transparent" Foreground="#6DD58C" FontFamily="Consolas" FontSize="12" BorderThickness="0" IsReadOnly="True"/>
                </ScrollViewer>
            </Grid>
        </Border>
    </Grid>
</Window>
"@

$reader = [System.Xml.XmlNodeReader]::new($xaml)
$window = [System.Windows.Markup.XamlReader]::Load($reader)

# Find UI controls
$txtDeviceName = $window.FindName("TxtDeviceName")
$txtDeviceInfo = $window.FindName("TxtDeviceInfo")
$txtLogs = $window.FindName("TxtLogs")
$btnRefresh = $window.FindName("BtnRefresh")
$btnDeployLatest = $window.FindName("BtnDeployLatest")
$btnPickApk = $window.FindName("BtnPickApk")
$btnLaunchApp = $window.FindName("BtnLaunchApp")
$btnScreenshot = $window.FindName("BtnScreenshot")

function Append-Log($msg) {
    $time = (Get-Date).ToString("HH:mm:ss")
    $txtLogs.Text += "[$time] $msg`r`n"
    $txtLogs.ScrollToEnd()
}

function Check-Device {
    Append-Log "Querying ADB devices..."
    if (-not (Test-Path $adbPath)) {
        $txtDeviceName.Text = "ADB Not Found"
        $txtDeviceInfo.Text = "Please ensure Android SDK platform-tools is installed."
        Append-Log "Error: adb.exe not found at $adbPath"
        return
    }

    $raw = & $adbPath devices -l
    $lines = $raw -split "`r?`n" | Where-Object { $_ -match "^\S+\s+device\b" }

    if ($lines) {
        $first = $lines[0]
        $serial = ($first -split "\s+")[0]
        $model = if ($first -match "model:(\S+)") { $matches[1] -replace "_", " " } else { "Android Device" }
        $product = if ($first -match "product:(\S+)") { $matches[1] } else { "" }

        $txtDeviceName.Text = "$model ($product)"
        $txtDeviceInfo.Text = "Serial: $serial | Status: Authorized & Online"
        Append-Log "Found connected device: $model ($serial)"
    } else {
        $txtDeviceName.Text = "No Device Connected"
        $txtDeviceInfo.Text = "Connect your Pixel via USB or Wireless ADB."
        Append-Log "No authorized device detected via adb devices."
    }
}

function Install-ApkFile($apkPath) {
    if (-not (Test-Path $apkPath)) {
        Append-Log "Error: APK file not found at $apkPath"
        [System.Windows.MessageBox]::Show("APK not found: $apkPath", "File Error", "OK", "Error")
        return
    }

    Append-Log "Starting installation of: $(Split-Path $apkPath -Leaf)"
    Append-Log "Running adb install -r..."
    $result = & $adbPath install -r $apkPath
    Append-Log ($result -join "`r`n")

    if ($result -match "Success") {
        Append-Log "APK installation SUCCEEDED!"
        Append-Log "Restarting Pixel (Un)Fold activity..."
        & $adbPath shell am force-stop com.pixel.foldpaper
        Start-Sleep -Milliseconds 200
        & $adbPath shell am start -a android.intent.action.MAIN -c android.intent.category.LAUNCHER -n com.pixel.foldpaper/.MainActivity
        Append-Log "App launched successfully on device."
        [System.Windows.MessageBox]::Show("Installation Succeeded!`r`nApp launched on your device.", "Success", "OK", "Information")
    } else {
        Append-Log "Installation output did not confirm success. Check console above."
    }
}

$btnRefresh.Add_Click({
    Check-Device
})

$btnDeployLatest.Add_Click({
    if (Test-Path $defaultApk) {
        Install-ApkFile $defaultApk
    } else {
        Append-Log "Building APK via Gradle first..."
        & (Join-Path $projectDir "gradlew.bat") assembleDebug
        Install-ApkFile $defaultApk
    }
})

$btnPickApk.Add_Click({
    $dialog = [Microsoft.Win32.OpenFileDialog]::new()
    $dialog.Filter = "Android Package (*.apk)|*.apk|All Files (*.*)|*.*"
    $dialog.InitialDirectory = (Join-Path $projectDir "app\build\outputs\apk\debug")
    $dialog.Title = "Select APK File to Install"
    if ($dialog.ShowDialog()) {
        Install-ApkFile $dialog.FileName
    }
})

$btnLaunchApp.Add_Click({
    Append-Log "Launching com.pixel.foldpaper/.MainActivity..."
    & $adbPath shell am start -a android.intent.action.MAIN -c android.intent.category.LAUNCHER -n com.pixel.foldpaper/.MainActivity
    Append-Log "Launch intent sent."
})

$btnScreenshot.Add_Click({
    Append-Log "Capturing screenshot..."
    $saveDir = Join-Path $projectDir "screenshots"
    if (-not (Test-Path $saveDir)) { New-Item -ItemType Directory -Path $saveDir | Out-Null }
    $stamp = (Get-Date).ToString("yyyyMMdd_HHmmss")
    $saveFile = Join-Path $saveDir "screen_$stamp.png"

    & $adbPath shell screencap -d 4619827677550801153 -p /sdcard/screen.png
    & $adbPath pull /sdcard/screen.png $saveFile
    & $adbPath shell rm /sdcard/screen.png

    Append-Log "Screenshot saved to: $saveFile"
    Start-Process $saveFile
})

$window.Add_Loaded({
    Append-Log "Pixel (Un)Fold Deployer Initialized."
    Check-Device
})

$window.ShowDialog() | Out-Null
