import chisel3._
import chisel3.util._

class Stage1_IF(program: Seq[Int], fpga: Boolean) extends Module {
  val io = IO(new Bundle{
    val jump = Input(Bool())
    val jump_offset = Input(SInt(32.W))
    val instruction = Output(UInt(32.W))
    val pc = Output(UInt(32.W))
  })

  val im = Module(new MemoryInstruction(program, fpga))
  val pc_reg = RegInit(-4.S(32.W))
  val pc = Mux(io.jump, (pc_reg + io.jump_offset).asUInt, (pc_reg + 4.S).asUInt)
  pc_reg := pc.asSInt
  //pc_reg := -4.S

  im.io.pc := pc
  io.instruction := im.io.instruction
  io.pc := pc
}
