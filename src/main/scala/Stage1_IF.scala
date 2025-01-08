import chisel3._
import chisel3.util._

class Stage1_IF(fpga: Boolean, mem_size: Int) extends Module {
  val io = IO(new Bundle{
    val jump = Input(Bool())
    val jump_offset = Input(SInt(32.W))
    val instruction = Output(UInt(32.W))
  })

  val im = new MemoryInstruction(fpga, mem_size)
  val pc_reg = RegInit(-4.S(32.W))
  val pc = Mux(io.jump, (pc_reg + io.jump_offset).asUInt, (pc_reg + 4.S).asUInt)

  im.io.pc := pc
  io.instruction := im.io.instruction
}
