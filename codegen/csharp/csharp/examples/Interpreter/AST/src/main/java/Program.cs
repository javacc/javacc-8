using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Bug002
{
    class Program
    {
        static void Main(string[] args)
        {
            TextReader input = Console.In;
            TextWriter output = Console.Out;
            TextWriter error = Console.Error;
            TextReader prevInput = null;
            TextWriter prevOutput = null;
            TextWriter prevError = null;
            if (args.Length == 3)
            {
                prevInput = input; input = new StreamReader(args[0]);
                prevOutput = output; output = new StreamWriter(args[1]);
                prevError = error; error = new StreamWriter(args[2]);
                Console.SetIn(input);
                Console.SetOut(output);
                Console.SetError(error);
            }
            try
            {
                Bug parser = new Bug(input);
                parser.EnumerationItem();
            }
            catch (Exception e)
            {
                error.Write(e.Message);
            }
            finally
            {
                if (prevInput != null) Console.SetIn(prevInput);
                if (prevOutput != null) Console.SetOut(prevOutput);
                if (prevError != null) Console.SetError(prevError);
            }
        }
    }
}
