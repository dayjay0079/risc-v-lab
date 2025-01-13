import chisel3._
import chisel3.util._

class Stage1_IF(program: Seq[Int], fpga: Boolean) extends Module {
  val io = IO(new Bundle{
    val jump = Input(Bool())
    val jump_offset = Input(SInt(32.W))
    val branch_enable = Input(Bool())
    val branch_pc = Input(UInt(32.W))

    val instruction = Output(UInt(32.W))
    val pc_reg = Output(UInt(32.W))
    val pc = Output(UInt(32.W)) // Only passed for debugging
  })

  // Initialize instruction memory with given program
  val instruction_memory = Module(new MemoryInstruction(program, fpga))

  // Set up program counter circuit
  val pc_reg = RegInit(-4.S(32.W))
  val pc = Mux(io.jump, (pc_reg + io.jump_offset).asUInt, Mux(io.branch_enable, io.branch_pc, (pc_reg + 4.S).asUInt))
  pc_reg := pc.asSInt

  // Read instruction
  instruction_memory.io.pc := pc

  // Output
  io.instruction := instruction_memory.io.instruction
  io.pc := pc
  io.pc_reg := pc_reg.asUInt

  // Initialization delay
  val init_reg = RegInit(0.U(3.W))
  val init_delay = init_reg <= 5.U
  when (init_delay) {
    pc_reg := -4.S
    io.instruction := 0.U
    init_reg := init_reg + 1.U
  }
}
