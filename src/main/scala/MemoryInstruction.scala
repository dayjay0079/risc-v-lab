import chisel3._
import chisel3.util._

class MemoryInstruction(fpga: Boolean, mem_size: Int) extends Module {
  val io = IO(new Bundle {
    val pc = Input(UInt(32.W))
    val instruction = Output(UInt(32.W))
  })

  // Define a method to handle unified memory access
  def readMemory(addr: UInt): UInt = {
    if (fpga) {
      IM.asInstanceOf[SyncReadMem[UInt]].read(addr)
    } else {
      IM.asInstanceOf[Vec[UInt]](addr)
    }
  }

  // Declare IM as AnyRef (parent type for both SyncReadMem and Vec)
  private val IM: AnyRef = if (fpga) {
    SyncReadMem(mem_size, UInt(32.W)) // Type: SyncReadMem
  } else {
    RegInit(VecInit(Seq.fill(mem_size)(0.U(32.W)))) // Type: Vec
  }

  // Example usage of readMemory
  io.instruction := readMemory(io.pc)
}
