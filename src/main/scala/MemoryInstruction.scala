import chisel3._
import chisel3.util._

class MemoryInstruction(program: Seq[Int], fpga: Boolean) extends Module {
  val io = IO(new Bundle {
    val pc = Input(UInt(32.W))
    val instruction = Output(UInt(32.W))
  })

  // Declare IM as AnyRef (parent type for both SyncReadMem and Vec)
  private val IM = VecInit(program.map(value => (value & 0xFFFFFFFFL).asUInt(32.W))) // Type: Vec
  
  // Example usage of readMemory
  io.instruction := RegNext(IM((io.pc >> 2.U).asUInt))
}
