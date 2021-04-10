using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
namespace IDL {
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
                prevInput = input; input = new StreamReader(@args[0]);Console.SetIn(input);
                prevOutput = output; output = new StreamWriter(@args[1]);Console.SetOut(output);
                prevError = error; error = new StreamWriter(@args[2]); Console.SetError(error);    
            }
            try
            {
                IDLParser parser = new IDLParser(input);
                //parser.enable_tracing();
                parser.specification();
                output.WriteLine("IDL Parser:  IDL file parsed successfully.");
            }
            catch (Exception e)
            {
                error.Write(e);
            }
            finally
            {
            if (prevInput != null)
            {
                input.Close();Console.SetIn(prevInput);
            }
            if (prevOutput != null)
            {
                output.Close();Console.SetOut(prevOutput);
            }
            if (prevError != null)
            {
                error.Close();Console.SetError(prevError);
            }
        }
    }
    }
}