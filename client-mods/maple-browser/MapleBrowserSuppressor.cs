using System;
using System.Reflection;

[assembly: AssemblyTitle("ComfyStory Maple Browser Suppressor")]
[assembly: AssemblyDescription("Prevents the stock MapleStory news browser from opening.")]
[assembly: AssemblyVersion("1.0.0.0")]

internal static class MapleBrowserSuppressor
{
    [STAThread]
    private static void Main()
    {
        // MapleStory only needs the helper process launch to succeed. Exiting here
        // prevents the stock browser from displaying or contacting Nexon's news site.
    }
}
