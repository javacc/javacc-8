using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

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
                prevInput = input; input = new StreamReader(@args[0]);
                prevOutput = output; output = new StreamWriter(@args[1]);
                prevError = error; error = new StreamWriter(@args[2]);
            
            }
            try
            {
                Bug parser = new Bug(input);
                parser.enable_tracing();
                parser.EnumerationItem();
                output.WriteLine("Parser :  file parsed successfully.");
            }
            catch (Exception e)
            {
                error.Write(e);
            }
            finally
            {
            if (prevInput != null)
            {
                input.Close();
                Console.SetIn(prevInput);
            }
            if (prevOutput != null)
            {
                output.Flush(); output.Close();
                Console.SetOut(prevOutput);
            }
            if (prevError != null)
            {
                error.Flush(); error.Close();
                Console.SetError(prevError);
            }
        }
    }
    }
