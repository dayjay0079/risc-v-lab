import chisel3._
import chisel3.util._

class MemoryData(fpga: Boolean, mem_size: Int) extends Module {
  val io = IO(new Bundle {
    val address = Input(UInt(32.W))
    val data_in = Input(SInt(32.W))
    val write_enable = Input(Bool())
    val data_out = Output(SInt(32.W))
  })

  // Define a method to handle unified memory access
  def readMemory(addr: UInt): SInt = {
    if (fpga) {
      memory.asInstanceOf[SyncReadMem[SInt]].read(addr)
    } else {
      RegNext(memory.asInstanceOf[Vec[SInt]](addr))
    }
  }

  // Declare memory as AnyRef (parent type for both SyncReadMem and Vec)
  private val memory: AnyRef = if (fpga) {
    SyncReadMem(mem_size, SInt(32.W)) // Type: SyncReadMem
  } else {
    RegInit(VecInit(Seq.fill(mem_size)(0.S(32.W)))) // Type: Vec
  }

  // Example usage of readMemory
  io.data_out := readMemory(io.address)
}
