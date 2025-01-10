import chisel3._
import chisel3.util._

class Stage1_IF(program: Seq[Int], fpga: Boolean) extends Module {
  val io = IO(new Bundle{
    val jump = Input(Bool())
    val jump_offset = Input(SInt(32.W))
    val instruction = Output(UInt(32.W))
    val pc = Output(UInt(32.W)) // Only passed for debugging
  })

  // Initialize instruction memory with given program
  val instruction_memory = Module(new MemoryInstruction(program, fpga))

  // Set up program counter circuit
  val isfirst = RegInit(0.B)
  val pc_reg = RegInit(-4.S(32.W))
  val pc = Mux(io.jump, (pc_reg + io.jump_offset).asUInt, Mux(isfirst, (pc_reg + 4.S).asUInt, 0.U))
  pc_reg := pc.asSInt

  when(!isfirst) {
    isfirst := 1.B
  }


  // Read instruction
  instruction_memory.io.pc := pc

  // Output
  io.instruction := instruction_memory.io.instruction
  io.pc := pc
}
